#!/usr/bin/env python3
"""Assemble a relocatable NetX release archive.

    python3 packaging/assemble.py \\
        --version 0.1.0 --os linux --arch x64 \\
        --dist build/install/nx \\
        --cmake-build build/nxsim \\
        --runtime build/runtime \\
        --out dist

The resulting archive unpacks to

    netx-<version>-<os>-<arch>/
    ├── bin/            nx, nx.bat, nxsim_cli
    ├── include/nxsim/  simulation.h … (for writing C++ testbenches)
    ├── lib/            *.jar, cmake/nxsim/*, runtime/ (bundled JVM)
    └── share/          (optional)

`--runtime` is a jlink image produced from the same JDK the jars were compiled
with; see RELEASING.md. If it is omitted the launcher falls back to `java` on
PATH (or `$JAVA_HOME`), which means the user needs a matching JDK installed.
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import tarfile
import zipfile
from pathlib import Path

LAUNCHER_SH = """#!/bin/sh
# NetX launcher — prefers the JVM shipped with this package.
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
if [ -x "$APP_HOME/lib/runtime/bin/java" ]; then
    JAVA="$APP_HOME/lib/runtime/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi
exec "$JAVA" -cp "$APP_HOME/lib/*" pascal.netx.Main "$@"
"""

LAUNCHER_BAT = """@echo off
setlocal
set "APP_HOME=%~dp0.."
if exist "%APP_HOME%\\lib\\runtime\\bin\\java.exe" (
    set "JAVA=%APP_HOME%\\lib\\runtime\\bin\\java.exe"
) else (
    set "JAVA=java"
)
"%JAVA%" -cp "%APP_HOME%\\lib\\*" pascal.netx.Main %*
"""


def stage_tree(args: argparse.Namespace) -> Path:
    out = Path(args.out).resolve()
    stage = out / f"netx-{args.version}-{args.os}-{args.arch}"
    if stage.exists():
        shutil.rmtree(stage)
    (stage / "bin").mkdir(parents=True)
    (stage / "lib").mkdir(parents=True)

    dist_lib = Path(args.dist).resolve() / "lib"
    jars = sorted(dist_lib.glob("*.jar"))
    if not jars:
        sys.exit(f"error: no jars found in {dist_lib} — run './gradlew installDist' first")
    for jar in jars:
        shutil.copy2(jar, stage / "lib" / jar.name)

    if args.runtime:
        runtime = Path(args.runtime).resolve()
        if not (runtime / "bin").is_dir():
            sys.exit(f"error: {runtime} does not look like a jlink runtime image")
        shutil.copytree(runtime, stage / "lib" / "runtime", symlinks=True)

    if args.cmake_build and not args.skip_cmake:
        build = Path(args.cmake_build).resolve()
        if not build.is_dir():
            sys.exit(
                f"error: {build} not found — run './gradlew buildCMake' first, "
                "or pass --skip-cmake for a Java-only archive"
            )
        subprocess.run(
            ["cmake", "--install", str(build), "--prefix", str(stage)],
            check=True,
        )

    for name, body, mode in (("nx", LAUNCHER_SH, 0o755), ("nx.bat", LAUNCHER_BAT, 0o644)):
        target = stage / "bin" / name
        target.write_text(body.replace("\n", os.linesep if args.os == "windows" else "\n"))
        target.chmod(mode)

    return stage


def bundle_dlls(stage: Path, dll_dir: Path) -> list[str]:
    """Copy the runtime DLLs the executables need next to them.

    Windows/MinGW only: the exe links libstdc++/libgcc/libwinpthread/libgomp from
    the toolchain, which end users do not have. `objdump -p` lists what it imports
    and we copy whatever exists in ``dll_dir`` — system DLLs (KERNEL32.dll, …) are
    not in that directory, so they are skipped automatically.
    """
    copied: list[str] = []
    for exe in sorted((stage / "bin").glob("*.exe")):
        try:
            dump = subprocess.run(
                ["objdump", "-p", str(exe)],
                capture_output=True,
                text=True,
                check=True,
            ).stdout
        except (OSError, subprocess.CalledProcessError) as exc:
            sys.exit(f"error: --bundle-dlls needs objdump to inspect {exe.name}: {exc}")
        for name in re.findall(r"DLL Name: (\S+)", dump):
            source = dll_dir / name
            target = stage / "bin" / name
            if source.is_file() and not target.exists():
                shutil.copy2(source, target)
                copied.append(name)
    return copied



def archive(stage: Path, args: argparse.Namespace) -> Path:
    out = Path(args.out).resolve()
    out.mkdir(parents=True, exist_ok=True)
    if args.os == "windows":
        target = out / f"{stage.name}.zip"
        with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as zf:
            for path in sorted(stage.rglob("*")):
                if path.is_file():
                    zf.write(path, path.relative_to(out))
    else:
        target = out / f"{stage.name}.tar.gz"
        with tarfile.open(target, "w:gz") as tf:
            for path in sorted(stage.rglob("*")):
                tarinfo = tf.gettarinfo(str(path), arcname=str(path.relative_to(out)))
                if tarinfo.isfile():
                    tarinfo.mode = 0o755 if os.access(path, os.X_OK) else 0o644
                tf.addfile(tarinfo, path.open("rb") if tarinfo.isfile() else None)
    return target


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", required=True)
    parser.add_argument("--os", required=True, choices=["linux", "macos", "windows"])
    parser.add_argument("--arch", required=True, choices=["x64", "arm64", "aarch64"])
    parser.add_argument("--dist", default="build/install/nx")
    parser.add_argument("--cmake-build", default="build/nxsim")
    parser.add_argument(
        "--skip-cmake",
        action="store_true",
        help="produce a Java-only archive (no nxsim_cli / headers)",
    )
    parser.add_argument(
        "--bundle-dlls",
        metavar="DIR",
        help="copy non-system DLLs the executables need from DIR into bin/ (MinGW builds)",
    )
    parser.add_argument("--runtime", default="build/runtime")
    parser.add_argument("--out", default="dist")
    args = parser.parse_args()

    stage = stage_tree(args)
    if args.bundle_dlls:
        copied = bundle_dlls(stage, Path(args.bundle_dlls))
        print(f"bundled DLLs: {', '.join(copied) if copied else '(none needed)'}")
    target = archive(stage, args)
    size = target.stat().st_size / 1e6
    print(f"assembled {stage}")
    print(f"wrote     {target} ({size:.1f} MB)")


if __name__ == "__main__":
    main()
