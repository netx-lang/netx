"""
Usage:
    nx compile <source>
        [--compact] [--minimal]
        [--top=<top>] 
        [--output=<filename>]
    nx dump <source>
        [--top=<top>] 
        [--output=<filename>]
    nx visualize <source>
        [--top=<top>]
        [--depth=<depth>]
        [--output=<filename>]
    nx run <source>
        [--top=<top>]

Commands:
    compile   Compile the NetX source code into JSON.
    dump      Dump the NetX source code to Verilog.
    visualize Generate a DOT visualization of the NetX source code.
    run       Compile and start an interactive session with the simulator.

Arguments:
    SOURCE   The source file to compile, dump, or run.

Options:
    -o, --output=<filename>       Output file
    -c, --compact                 Compile the source code to compact JSON representation  (for compile only).
    -m, --minimal                 Print the minimal version of the JSON output (for compile only).
    -t, --top=<top>               Name of the top component
    -d, --depth=<depth>           Depth of the visualization (for visualize only).
"""
from docopt import docopt
import sys
import subprocess
from prompt_toolkit import PromptSession
from prompt_toolkit.completion import NestedCompleter

def build_command(args):
    command = ['java', '-jar', 'build/libs/netx-1.0-SNAPSHOT.jar']
    if args['compile']:
        command.append('compile')
    elif args['dump']:
        command.append('dump')
    elif args['visualize']:
        command.append('visualize')
    elif args['run']:
        command.append('compile')
        command.extend(['--minimal'])

    command.append(args['<source>'])
    if args['--compact']:
        command.append('--compact')
    if args['--minimal']:
        command.append('--minimal')
    if args['--top']:
        command.extend(['--top', args['--top']])
    if args['--output']:
        command.extend(['--output', args['--output']])
    if args['--depth']:
        command.extend(['--depth', args['--depth']])
    
    return command

def build_nested_completer(ports):
    port_dict = {port: None for port in ports}
    completer_dict = {
        'run': None,
        'exit': None,
        'get': port_dict,
        'set': port_dict
    }
    completer = NestedCompleter.from_nested_dict(completer_dict)
    return completer

def REPL(json):
    process = subprocess.Popen(
        ['./simulator/cpp-build/simulator', json],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    ports = process.stdout.readline().strip().split()
    session = PromptSession()
    completer = build_nested_completer(ports)
    try:
        while True:
            user_input = session.prompt(">> ", completer=completer)
            process.stdin.write(user_input.strip() + "\n")
            process.stdin.flush()
            if user_input.strip().lower() == "exit":
                break
            response = process.stdout.readline()
            print(response.strip())

        return_code = process.wait()
        if return_code != 0:
            print(f"Executable exited with error code {return_code}")
            print(f"Error: {process.stderr.read().strip()}")
    except KeyboardInterrupt:
        print("")
        print("Ctrl+C pressed. Exiting.")
    except Exception as e:
        print(f"Error running executable: {e}")
    finally:
        process.terminate()
        process.wait() 
        print("Simulator terminated.")
   
def main():
    args = docopt(__doc__)
    command = build_command(args)
    result = subprocess.run(command, capture_output=True, text=True, check=True)
    
    if result.stderr:
        print(result.stderr, file=sys.stderr)

    if args['run']:
        REPL(result.stdout)
    elif not args['--output']:
        print(result.stdout)

if __name__ == "__main__":
    main()