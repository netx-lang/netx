"""
Usage:
    hs compile <source>
        [--compact] 
        [--top=<name>] 
        [--output=<filename>]
    hs run <source> [--top=<name>]

Options:
    -o, --output=<filename>       Output file
    -c, --compact                 Compile the source code to compact JSON representation  [Default]
    -e, --expanded                Compile the source code to expanded JSON representation
    -t, --top=<name>              Name of the top component
    -a, --arguments=<arguments>   Arguments to pass to the compiler
"""
from docopt import docopt
import subprocess

def main():
    args = docopt(__doc__)
    if args['compile']:
        compile(args)
    elif args['run']:
        run(args)

def compile(args):
    compact = '1' if args['--compact'] else '0'
    top     = 'top' if args['--top'] is None else args['--top']
    output  = '-' if args['--output'] is None else args['--output']
    command = ['java', '-jar', 'build/libs/hardscript-1.0-SNAPSHOT.jar'] + [args["<source>"], compact, top, output]
    result  = subprocess.run(command, capture_output=True, text=True, check=True)
    print(result.stderr)
    if output == "-":
        print(result.stdout)

def run(args):
    compact = '0'
    top     = 'top' if args['--top'] is None else args['--top']
    output  = '-' 
    command = ['java', '-jar', 'build/libs/hardscript-1.0-SNAPSHOT.jar'] + [args["<source>"], compact, top, output]
    result  = subprocess.run(command, capture_output=True, text=True, check=True)
    try:
        process = subprocess.Popen(
            ['./hson/cpp-build/simulator', result.stdout],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )

        while True:
            user_input = input(">> ")
            process.stdin.write(user_input + "\n")
            process.stdin.flush()
            if user_input.strip().lower() == "exit":
                break

            response = process.stdout.readline()
            print(response.strip())

        return_code = process.wait()
        if return_code != 0:
            print(f"Executable exited with error code {return_code}")
            print(f"Error: {process.stderr.read().strip()}")

    except Exception as e:
        print(f"Error running executable: {e}")

    finally:
        print("Terminating the simulator...")
        process.terminate()
        process.wait() 
        print("Simulator terminated.")
   
if __name__ == "__main__":
    main()
