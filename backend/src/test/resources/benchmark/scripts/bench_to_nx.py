#!/usr/bin/env python3
"""
Convert BENCH netlists into .nx format.

Usage:
  python scripts/bench_to_nx.py <input.bench> [-o <output.nx>]

- Parses INPUT, OUTPUT declarations and gate definitions
- Produces a single NX component with bit(1) wires for all ports and nets
- Maps logic gates (NOT, AND, NAND, OR, NOR, XOR, XNOR, BUF, DFF) to NX primitives
- Sanitizes BENCH identifiers into NX-safe names

Supports:
  - ISCAS85/ITC99 gate-level netlists in .bench format
"""

import argparse
import os
import re
from typing import List, Tuple, Dict, Set

# -----------------------------
# Identifier sanitization
# -----------------------------

def sanitize_ident(name: str) -> str:
    """Convert BENCH identifiers into NX-safe names.
    - Replace non [A-Za-z0-9_] with '_'
    - If starts with a digit, prefix with 'n_'
    - Collapse multiple underscores
    """
    s = name.strip()
    # Replace all non-word chars with underscore
    s = re.sub(r"[^A-Za-z0-9_]", "_", s)
    # Collapse consecutive underscores
    s = re.sub(r"_+", "_", s)
    # Trim underscores at ends
    s = s.strip('_') or 'n'
    if re.match(r"^[0-9]", s):
        s = 'n_' + s
    return s

# -----------------------------
# BENCH parser
# -----------------------------

def parse_bench_file(text: str) -> Tuple[List[str], List[str], Dict[str, Tuple[str, List[str]]]]:
    """
    Parse a .bench file.
    
    Returns:
        (inputs, outputs, gates)
        gates: dict mapping gate_output -> (gate_type, [gate_inputs])
    """
    inputs: List[str] = []
    outputs: List[str] = []
    gates: Dict[str, Tuple[str, List[str]]] = {}
    
    lines = text.split('\n')
    for line in lines:
        # Remove comments
        line = re.sub(r'#.*$', '', line).strip()
        if not line:
            continue
            
        # Parse INPUT
        m = re.match(r'^INPUT\s*\(\s*([^)]+)\s*\)$', line)
        if m:
            name = sanitize_ident(m.group(1))
            inputs.append(name)
            continue
            
        # Parse OUTPUT
        m = re.match(r'^OUTPUT\s*\(\s*([^)]+)\s*\)$', line)
        if m:
            name = sanitize_ident(m.group(1))
            outputs.append(name)
            continue
            
        # Parse gate definitions: name = GATE(inputs)
        # Format: output_name = GATE_TYPE(input1, input2, ...)
        m = re.match(r'^([^=]+)\s*=\s*([A-Z_]+)\s*\(\s*([^)]*)\s*\)$', line)
        if m:
            output_name = sanitize_ident(m.group(1))
            gate_type = m.group(2).upper()
            input_blob = m.group(3)
            
            # Parse comma-separated inputs
            input_list = [sanitize_ident(inp) for inp in input_blob.split(',') if inp.strip()]
            
            gates[output_name] = (gate_type, input_list)
            continue
    
    return inputs, outputs, gates

def extract_all_wires(inputs: List[str], outputs: List[str], gates: Dict[str, Tuple[str, List[str]]]) -> Set[str]:
    """Get all wires used in the circuit."""
    wires = set(inputs) | set(outputs) | set(gates.keys())
    for gate_type, input_list in gates.values():
        wires.update(input_list)
    return wires

# -----------------------------
# NX generation
# -----------------------------

def generate_nx(mod_name: str, inputs: List[str], outputs: List[str], 
                gates: Dict[str, Tuple[str, List[str]]]) -> str:
    """Generate NX component from parsed BENCH data."""

    # Detect sequential elements
    has_dff = any(g[0] == 'DFF' for g in gates.values())

    # If the design has DFFs, add a clock input so we can model them
    clk_name = 'clk'
    inputs_with_clk = list(inputs)
    if has_dff and clk_name not in inputs_with_clk:
        inputs_with_clk.append(clk_name)

    # Get all wires (include clk so it is declared, but mark its type separately)
    all_wires = extract_all_wires(inputs_with_clk, outputs, gates)
    internal_wires = all_wires - set(inputs_with_clk) - set(outputs)

    # Component signature
    sig_in = ', '.join(sorted(set(inputs_with_clk)))
    sig_out = ', '.join(sorted(set(outputs)))

    lines: List[str] = []
    lines.append(f"component {mod_name} : [{sig_in}] -> [{sig_out}] {{")

    # Declare clock separately when present
    if has_dff:
        lines.append(f"  wire {clk_name} of clock;")

    # Declare bit(1) input wires (excluding the clock)
    bit_inputs = [w for w in inputs_with_clk if w != clk_name]
    if bit_inputs:
        lines.append(f"  wire {', '.join(sorted(set(bit_inputs)))} of bit(1);")

    # Declare output wires
    if outputs:
        lines.append(f"  wire {', '.join(sorted(set(outputs)))} of bit(1);")

    # Declare internal wires (bit only; clock is already declared)
    bit_internal = [w for w in internal_wires if w != clk_name]
    if bit_internal:
        lines.append(f"  wire {', '.join(sorted(bit_internal))} of bit(1);")

    lines.append("")

    # Emit gate assignments in deterministic order
    for gate_output in sorted(gates.keys()):
        gate_type, gate_inputs = gates[gate_output]

        if gate_type == 'BUF':
            # Buffer: output = input
            if gate_inputs:
                lines.append(f"  {gate_output} <> {gate_inputs[0]};")

        elif gate_type == 'NOT':
            # NOT: output = NOT(input)
            if gate_inputs:
                lines.append(f"  {gate_output} <> NOT <> {gate_inputs[0]};")

        elif gate_type == 'AND':
            # AND: output = AND(inputs)
            expr = f"AND <> [{', '.join(gate_inputs)}]"
            lines.append(f"  {gate_output} <> {expr};")

        elif gate_type == 'NAND':
            # NAND: output = NOT(AND(inputs))
            expr = f"NOT <> AND <> [{', '.join(gate_inputs)}]"
            lines.append(f"  {gate_output} <> {expr};")

        elif gate_type == 'OR':
            # OR: output = OR(inputs)
            expr = f"OR <> [{', '.join(gate_inputs)}]"
            lines.append(f"  {gate_output} <> {expr};")

        elif gate_type == 'NOR':
            # NOR: output = NOT(OR(inputs))
            expr = f"NOT <> OR <> [{', '.join(gate_inputs)}]"
            lines.append(f"  {gate_output} <> {expr};")

        elif gate_type == 'XOR':
            # XOR: synthesize
            if len(gate_inputs) == 1:
                lines.append(f"  {gate_output} <> {gate_inputs[0]};")
            elif len(gate_inputs) == 2:
                expr = f"OR <> [AND <> [{gate_inputs[0]}, NOT <> {gate_inputs[1]}], AND <> [NOT <> {gate_inputs[0]}, {gate_inputs[1]}]]"
                lines.append(f"  {gate_output} <> {expr};")
            else:
                # Multi-input XOR: chain pairwise
                acc = gate_inputs[0]
                for i in range(1, len(gate_inputs)):
                    acc = f"OR <> [AND <> [{acc}, NOT <> {gate_inputs[i]}], AND <> [NOT <> {acc}, {gate_inputs[i]}]]"
                lines.append(f"  {gate_output} <> {acc};")

        elif gate_type == 'XNOR':
            # XNOR: NOT(XOR(inputs))
            if len(gate_inputs) == 1:
                lines.append(f"  {gate_output} <> NOT <> {gate_inputs[0]};")
            elif len(gate_inputs) == 2:
                expr = f"NOT <> OR <> [AND <> [{gate_inputs[0]}, NOT <> {gate_inputs[1]}], AND <> [NOT <> {gate_inputs[0]}, {gate_inputs[1]}]]"
                lines.append(f"  {gate_output} <> {expr};")
            else:
                # Multi-input XNOR
                acc = gate_inputs[0]
                for i in range(1, len(gate_inputs)):
                    acc = f"OR <> [AND <> [{acc}, NOT <> {gate_inputs[i]}], AND <> [NOT <> {acc}, {gate_inputs[i]}]]"
                lines.append(f"  {gate_output} <> NOT <> {acc};")

        elif gate_type == 'DFF':
            # Model a flip-flop using a clocked register.
            # The BENCH format omits clock, so we expose a generic clk input.
            if gate_inputs:
                lines.append(f"  {gate_output} <> REGISTER(pos_edge = true) <> [{clk_name}#, {gate_inputs[0]}];")

        else:
            # Unknown gate type - emit as comment and pass-through for visibility
            lines.append(f"  // Unknown gate type: {gate_type}")
            if gate_inputs:
                lines.append(f"  {gate_output} <> {gate_inputs[0]};")

    lines.append("}")
    return '\n'.join(lines) + '\n'

# -----------------------------
# CLI
# -----------------------------

def main():
    ap = argparse.ArgumentParser(
        description='Convert BENCH netlists to .nx format'
    )
    ap.add_argument('input', help='Input BENCH netlist file')
    ap.add_argument('-o', '--output', help='Output .nx file (default: same name as input)')
    args = ap.parse_args()
    
    # Read input file
    with open(args.input, 'r') as f:
        text = f.read()
    
    # Parse BENCH file
    inputs, outputs, gates = parse_bench_file(text)
    
    # Generate component name from filename
    base_name = os.path.splitext(os.path.basename(args.input))[0]
    mod_name = sanitize_ident(base_name)
    
    # Generate NX
    nx_content = generate_nx(mod_name, inputs, outputs, gates)
    
    # Determine output path
    out_path = args.output
    if not out_path:
        out_path = os.path.join(
            os.path.dirname(args.input),
            base_name + '.nx'
        )
    
    # Write output
    with open(out_path, 'w') as f:
        f.write(nx_content)
    
    print(f"Successfully converted {args.input} to {out_path}")
    print(f"  Inputs: {len(inputs)}")
    print(f"  Outputs: {len(outputs)}")
    print(f"  Gates: {len(gates)}")

if __name__ == '__main__':
    main()
