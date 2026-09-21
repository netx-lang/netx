"""Runner contract tests using isolated repositories and executable tool doubles."""
import contextlib
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

import benchmark


class RunnerTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.repo = self.root / "repo"
        self.repo.mkdir()
        subprocess.run(["git", "init", "-q", str(self.repo)], check=True)
        subprocess.run(["git", "-C", str(self.repo), "-c", "user.name=Test", "-c",
                        "user.email=test@example.invalid", "commit", "-qm", "fixture", "--allow-empty"], check=True)
        self.corpus = self.repo / "corpus"
        (self.corpus / "rv32i").mkdir(parents=True)
        (self.corpus / "rv32i/main.cpp").touch()
        (self.corpus / "rv32i/_netx.toml").touch()
        self.nx = self.tool("nx", """
if '--version' in sys.argv:
    print('fixture nx')
else:
    Path(sys.argv[sys.argv.index('--output') + 1]).write_text('{"fixture": true}')
""")
        self.cxx = self.tool("cxx", """
if '--version' in sys.argv:
    print('fixture cxx')
else:
    include = next(a[2:] for a in sys.argv if a.startswith('-I'))
    assert (Path(include) / 'nxsim').is_symlink(), include
    binary = Path(sys.argv[sys.argv.index('-o') + 1])
    binary.write_text('#!' + sys.executable + '\\nimport json,sys\\nassert json.load(sys.stdin)["fixture"]\\n')
    binary.chmod(0o755)
""")
        self.vg = self.tool("valgrind", """
if '--version' in sys.argv:
    print('fixture valgrind')
else:
    import subprocess
    subprocess.run([sys.argv[-1]], check=True)
    out = next(a.split('=', 1)[1] for a in sys.argv if a.startswith('--callgrind-out-file='))
    Path(out).write_text('events: Dr Ir\\nsummary: 5 42\\n')
""")

    def tool(self, name, code):
        path = self.root / name
        path.write_text(f'#!{sys.executable}\nimport sys\nfrom pathlib import Path\n' + code)
        path.chmod(0o755)
        return str(path)

    def invoke(self, *extra):
        stdout = io.StringIO()
        with patch.object(benchmark, "ROOT", self.repo), patch.dict(os.environ, {"PATH": os.environ["PATH"]}, clear=True), contextlib.redirect_stdout(stdout):
            code = benchmark.main(["HEAD", "--benchmarks-dir", str(self.corpus), "--nx", self.nx,
                                   "--cxx", self.cxx, "--valgrind", self.vg, "--designs", "rv32i",
                                   "--metrics", "Ir", "--format", "json", "--output-dir", str(self.root / "results"), *extra])
        worktrees = subprocess.check_output(["git", "-C", str(self.repo), "worktree", "list", "--porcelain"], text=True)
        self.assertEqual(worktrees.count("worktree "), 1)
        return code, stdout.getvalue()

    def test_success_stdin_include_path_and_machine_output(self):
        code, output = self.invoke()
        self.assertEqual(code, 0)
        self.assertEqual(json.loads(output)["benchmarks"][0]["head"], {"Ir": 42})
        self.assertTrue((self.root / "results/bench.json").is_file())

    def test_failed_driver_cannot_publish(self):
        self.vg = self.tool("valgrind", "print('fixture') if '--version' in sys.argv else sys.exit(7)")
        code, output = self.invoke()
        self.assertEqual(code, 1)
        self.assertEqual(json.loads(output)["benchmarks"][0]["status"], "failed")
        self.assertFalse((self.root / "results/bench.json").exists())
        self.assertTrue((self.root / "results/rv32i/run-head-0.log").exists())

    def test_timeout_cannot_publish(self):
        self.vg = self.tool("valgrind", "print('fixture') if '--version' in sys.argv else __import__('time').sleep(30)")
        code, _ = self.invoke("--timeout", "0.5")
        self.assertEqual(code, 1)
        self.assertFalse((self.root / "results/bench.json").exists())

    def test_wall_comparison_and_samples(self):
        code, output = self.invoke("HEAD", "--mode", "wall", "--iters", "2")
        self.assertEqual(code, 0)
        row = json.loads(output)["benchmarks"][0]
        self.assertEqual(len(row["samples_ms"]["base"]), 2)
        self.assertGreater(row["ratio"]["ms"], 0)

    def test_missing_metric_rejects_partial_summary(self):
        code, _ = self.invoke("--metrics", "Ir D1mr")
        self.assertEqual(code, 1)
        self.assertFalse((self.root / "results/bench.json").exists())

    def test_gh_action_failure_has_no_stdout(self):
        self.vg = self.tool("valgrind", "print('fixture') if '--version' in sys.argv else sys.exit(7)")
        code, output = self.invoke("--format", "gh-action")
        self.assertEqual(code, 1)
        self.assertEqual(output, "")


if __name__ == "__main__":
    unittest.main()
