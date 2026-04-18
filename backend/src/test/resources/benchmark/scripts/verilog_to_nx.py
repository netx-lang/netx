#!/usr/bin/env python3
"""
Convert Verilog combinational netlists (assign-based or gate-based) into .nx format.

Usage:
  python scripts/verilog_to_nx.py <input.v> [-o <output.nx>]

- Parses module, inputs/outputs/wires, assigns, and gate instantiations
- Produces a single NX component with bit(1) wires for all ports and nets
- Maps ~, &, |, ^ into NOT/AND/OR/XOR (XOR implemented via OR/AND/NOT)
- Maps gates (and, or, nand, nor, not, xor, buf) to NX primitives
- Sanitizes Verilog identifiers (escaped names, parentheses) into NX-safe names

Supports:
  - LGSynth89/91: assign-based combinational netlists
  - ISCAS85/ITC99: gate-level instantiations
"""

import argparse
import os
import re
from typing import List, Tuple, Union

# -----------------------------
# Identifier sanitization
# -----------------------------

def sanitize_ident(name: str) -> str:
    """Convert Verilog identifiers (including escaped ones) into NX-safe names.
    - Strip leading backslash and trailing whitespace for escaped ids
    - Replace non [A-Za-z0-9_] with '_'
    - If starts with a digit, prefix with 'n_'
    - Collapse multiple underscores
    """
    s = name.strip()
    if s.startswith('\\'):
        # Escaped identifier ends at whitespace per Verilog, but file likely provides it as a token already
        s = s[1:].strip()
    # Replace constructs like (nn) and [nn] with _nn
    s = re.sub(r"[\[\(]", "_", s)
    s = re.sub(r"[\]\)]", "", s)
    # Replace all other non-word chars with underscore
    s = re.sub(r"[^A-Za-z0-9_]", "_", s)
    # Collapse consecutive underscores
    s = re.sub(r"_+", "_", s)
    # Trim underscores at ends
    s = s.strip('_') or 'n'
    if re.match(r"^[0-9]", s):
        s = 'n_' + s
    return s

# -----------------------------
# Lexer for boolean expressions
# -----------------------------

Token = Tuple[str, str]  # (type, value)

def tokenize(expr: str) -> List[Token]:
    tokens: List[Token] = []
    i = 0
    n = len(expr)
    while i < n:
        c = expr[i]
        if c.isspace():
            i += 1
            continue
        if c in '()':
            tokens.append(('LPAREN' if c == '(' else 'RPAREN', c))
            i += 1
            continue
        if c in '~&|^':
            t = {'~': 'NOT', '&': 'AND', '|': 'OR', '^': 'XOR'}[c]
            tokens.append((t, c))
            i += 1
            continue
        if c == '\\':
            # Escaped identifier: \... until whitespace or comma
            # Parentheses may be part of the escaped name (e.g., \V32(27))
            j = i + 1
            while j < n and not expr[j].isspace() and expr[j] != ',':
                j += 1
            ident = expr[i:j]
            tokens.append(('IDENT', ident))
            i = j
            continue
        if c.isalnum() or c == '_' or c == '$':
            # Identifier or number literal like 1'b0
            j = i + 1
            # Allow alnum, underscore, dollar, quotes, and brackets within names.
            # Do NOT include parentheses here; they are structural tokens.
            while j < n and (expr[j].isalnum() or expr[j] in "_'$[]"):
                j += 1
            ident = expr[i:j]
            tokens.append(('IDENT', ident))
            i = j
            continue
        if c == ',':
            tokens.append(('COMMA', ','))
            i += 1
            continue
        # Unknown char; skip
        i += 1
    return tokens

# -----------------------------
# Recursive descent parser
# Grammar:
#   expr := or_expr
#   or_expr := and_expr (OR and_expr)*
#   and_expr := xor_expr (AND xor_expr)*
#   xor_expr := unary (XOR unary)*
#   unary := NOT unary | primary
#   primary := IDENT | '(' expr ')'
# -----------------------------

class Node:
    pass

class Var(Node):
    def __init__(self, name: str):
        self.name = name

class Const(Node):
    def __init__(self, value: int):
        self.value = value  # 0 or 1

class Not(Node):
    def __init__(self, child: Node):
        self.child = child

class And(Node):
    def __init__(self, children: List[Node]):
        self.children = children

class Or(Node):
    def __init__(self, children: List[Node]):
        self.children = children

class Xor(Node):
    def __init__(self, children: List[Node]):
        self.children = children

class Parser:
    def __init__(self, tokens: List[Token]):
        self.toks = tokens
        self.pos = 0

    def peek(self, k: int = 0) -> Token:
        if self.pos + k < len(self.toks):
            return self.toks[self.pos + k]
        return ('EOF', '')

    def eat(self, ttype: str = None) -> Token:
        tok = self.peek()
        if ttype and tok[0] != ttype:
            raise ValueError(f"Expected {ttype}, found {tok}")
        self.pos += 1
        return tok

    def parse(self) -> Node:
        return self.parse_or()

    def parse_or(self) -> Node:
        left = self.parse_and()
        children = [left]
        while self.peek()[0] == 'OR':
            self.eat('OR')
            right = self.parse_and()
            children.append(right)
        return left if len(children) == 1 else Or(children)

    def parse_and(self) -> Node:
        left = self.parse_xor()
        children = [left]
        while self.peek()[0] == 'AND':
            self.eat('AND')
            right = self.parse_xor()
            children.append(right)
        return left if len(children) == 1 else And(children)

    def parse_xor(self) -> Node:
        left = self.parse_unary()
        children = [left]
        while self.peek()[0] == 'XOR':
            self.eat('XOR')
            right = self.parse_unary()
            children.append(right)
        return left if len(children) == 1 else Xor(children)

    def parse_unary(self) -> Node:
        if self.peek()[0] == 'NOT':
            self.eat('NOT')
            return Not(self.parse_unary())
        return self.parse_primary()

    def parse_primary(self) -> Node:
        tok = self.peek()
        if tok[0] == 'LPAREN':
            self.eat('LPAREN')
            node = self.parse_or()
            self.eat('RPAREN')
            return node
        if tok[0] == 'IDENT':
            ident = self.eat('IDENT')[1]
            # constants like 1'b0 or 1'b1
            if re.match(r"^[0-9]+\'[bB][01]$", ident):
                val = int(ident[-1])
                return Const(val)
            # bare 0/1
            if ident in {'0', '1'}:
                return Const(int(ident))
            return Var(ident)
        raise ValueError(f"Unexpected token: {tok}")

# -----------------------------
# NX emitter
# -----------------------------

def emit_nx(node: Node) -> str:
    if isinstance(node, Var):
        return sanitize_ident(node.name)
    if isinstance(node, Const):
        return "1'd1" if node.value == 1 else "1'd0"
    if isinstance(node, Not):
        return f"NOT <> {emit_nx(node.child)}"
    if isinstance(node, And):
        parts = ', '.join(emit_nx(ch) for ch in node.children)
        return f"AND <> [{parts}]"
    if isinstance(node, Or):
        parts = ', '.join(emit_nx(ch) for ch in node.children)
        return f"OR <> [{parts}]"
    if isinstance(node, Xor):
        # Implement XOR via OR( AND(a, NOT(b)), AND(NOT(a), b) ) for pairwise
        def xor2(a: Node, b: Node) -> str:
            return f"OR <> [AND <> [{emit_nx(a)}, NOT <> {emit_nx(b)}], AND <> [NOT <> {emit_nx(a)}, {emit_nx(b)}]]"
        ch = node.children
        acc = emit_nx(ch[0])
        for i in range(1, len(ch)):
            # Build with previous acc as Var placeholder by wrapping into pseudo node
            acc_expr = f"{acc}"
            acc = f"OR <> [AND <> [{acc_expr}, NOT <> {emit_nx(ch[i])}], AND <> [NOT <> {acc_expr}, {emit_nx(ch[i])}]]"
        return acc
    raise TypeError(f"Unknown node type: {type(node)}")

# -----------------------------
# Verilog file parsing (module, ports, decls, assigns)
# -----------------------------

def parse_ports(header: str) -> List[str]:
    # Extract between first '(' and matching ')'
    m = re.search(r"module\s+([A-Za-z0-9_\\][^\(]*)\((.*)\)\s*;", header, re.S)
    if not m:
        raise ValueError("Failed to parse module header")
    ports_blob = m.group(2)
    # Split by commas, strip
    raw_ports = [p.strip() for p in ports_blob.split(',') if p.strip()]
    return raw_ports

def parse_decl_block(kind: str, text: str) -> List[str]:
    # Find all blocks like: input ... ; possibly multiline with commas
    names: List[str] = []
    for m in re.finditer(rf"\b{kind}\b\s*(.*?);", text, re.S):
        blob = m.group(1)
        parts = [p.strip() for p in blob.replace('\n', ' ').split(',')]
        for p in parts:
            if not p:
                continue
            # Remove type ranges if any (rare in these netlists)
            names.append(p)
    return names

def _split_top_level_commas(s: str) -> List[str]:
    parts: List[str] = []
    buf: List[str] = []
    depth = 0
    i = 0
    while i < len(s):
        c = s[i]
        if c == '(':
            depth += 1
            buf.append(c)
        elif c == ')':
            depth = max(0, depth - 1)
            buf.append(c)
        elif c == ',' and depth == 0:
            part = ''.join(buf).strip()
            if part:
                parts.append(part)
            buf = []
        else:
            buf.append(c)
        i += 1
    tail = ''.join(buf).strip()
    if tail:
        parts.append(tail)
    return parts

def parse_assigns(text: str) -> List[Tuple[str, str]]:
    assigns: List[Tuple[str, str]] = []
    for m in re.finditer(r"assign\s*(.*?);", text, re.S):
        blob = m.group(1)
        # Split by commas at top level only (outside parentheses)
        items = _split_top_level_commas(blob)
        for it in items:
            lm = re.match(r"(.*?)=\s*(.*)$", it, re.S)
            if not lm:
                continue
            lhs = lm.group(1).strip()
            rhs = lm.group(2).strip()
            assigns.append((lhs, rhs))
    return assigns

def parse_gates(text: str) -> List[Tuple[str, str, List[str]]]:
    """Parse gate instantiations like: and AND2_1 (output, input1, input2);
    Returns list of (gate_type, instance_name, [ports])
    """
    gates: List[Tuple[str, str, List[str]]] = []
    gate_types = {'and', 'nand', 'or', 'nor', 'xor', 'xnor', 'not', 'buf', 'bufif0', 'bufif1', 'notif0', 'notif1'}
    for gate_type in gate_types:
        # Match: gate_type instance_name (ports);
        pattern = rf"\b{gate_type}\s+([A-Za-z0-9_\\][^\(]*)\s*\((.*?)\)\s*;"
        for m in re.finditer(pattern, text, re.S):
            inst_name_raw = m.group(1).strip()
            ports_blob = m.group(2)
            # Parse ports: comma-separated, respecting escapes
            ports = [p.strip() for p in ports_blob.split(',') if p.strip()]
            gates.append((gate_type, inst_name_raw, ports))
    return gates

def extract_module(text: str) -> Tuple[str, List[str], List[str], List[str], List[Tuple[str, str]], List[Tuple[str, str, List[str]]]]:
    # Module name
    mm = re.search(r"module\s+([A-Za-z0-9_\\][^\(]*)\(", text)
    if not mm:
        raise ValueError("Module name not found")
    raw_name = mm.group(1).strip()
    mod_name = sanitize_ident(raw_name)
    # Header ports
    header_end = text.find(';')
    ports = parse_ports(text[:header_end+1])
    # Declarations, assigns, and gates
    body = text[header_end+1:]
    inputs = parse_decl_block('input', body)
    outputs = parse_decl_block('output', body)
    wires = parse_decl_block('wire', body)
    assigns = parse_assigns(body)
    gates = parse_gates(body)
    return mod_name, ports, inputs, outputs, wires, assigns, gates

# -----------------------------
# NX generation
# -----------------------------

def generate_nx(mod_name: str, ports: List[str], inputs: List[str], outputs: List[str], wires: List[str], assigns: List[Tuple[str, str]], gates: List[Tuple[str, str, List[str]]]) -> str:
    # Sanitize names
    s_inputs = [sanitize_ident(p) for p in inputs]
    s_outputs = [sanitize_ident(p) for p in outputs]
    s_wires = [sanitize_ident(p) for p in wires]

    # Component signature: inputs from parsed inputs, outputs from parsed outputs
    sig_in = ', '.join(sorted(set(s_inputs)))
    sig_out = ', '.join(sorted(set(s_outputs)))

    lines: List[str] = []
    lines.append(f"component {mod_name} : [{sig_in}] -> [{sig_out}] {{")

    # Declare wires as bit(1)
    if s_inputs:
        lines.append(f"  wire {', '.join(sorted(set(s_inputs)))} of bit(1);")
    if s_outputs:
        lines.append(f"  wire {', '.join(sorted(set(s_outputs)))} of bit(1);")
    # Internal wires that are not ports
    internal = [w for w in s_wires if w not in s_inputs and w not in s_outputs]
    if internal:
        lines.append(f"  wire {', '.join(sorted(set(internal)))} of bit(1);")

    lines.append("")

    # Emit assignments from assign statements
    for lhs_raw, rhs_raw in assigns:
        lhs = sanitize_ident(lhs_raw)
        expr_tokens = tokenize(rhs_raw)
        try:
            node = Parser(expr_tokens).parse()
        except Exception as e:
            raise RuntimeError(f"Failed to parse expression for {lhs_raw}: {rhs_raw}\nError: {e}")
        nx_expr = emit_nx(node)
        lines.append(f"  {lhs} <> {nx_expr};")

    # Emit gate instantiations
    for gate_type, inst_name_raw, gate_ports in gates:
        if not gate_ports:
            continue
        # First port is typically output, rest are inputs
        output = sanitize_ident(gate_ports[0])
        inputs_raw = gate_ports[1:]
        inputs_san = [sanitize_ident(p) for p in inputs_raw]

        if gate_type == 'buf':
            # Buffer: output = input
            lines.append(f"  {output} <> {inputs_san[0]};")
        elif gate_type == 'not':
            # NOT: output = NOT(input)
            lines.append(f"  {output} <> NOT <> {inputs_san[0]};")
        elif gate_type == 'and':
            # AND: output = AND(inputs)
            expr = f"AND <> [{', '.join(inputs_san)}]"
            lines.append(f"  {output} <> {expr};")
        elif gate_type == 'nand':
            # NAND: output = NOT(AND(inputs))
            expr = f"NOT <> AND <> [{', '.join(inputs_san)}]"
            lines.append(f"  {output} <> {expr};")
        elif gate_type == 'or':
            # OR: output = OR(inputs)
            expr = f"OR <> [{', '.join(inputs_san)}]"
            lines.append(f"  {output} <> {expr};")
        elif gate_type == 'nor':
            # NOR: output = NOT(OR(inputs))
            expr = f"NOT <> OR <> [{', '.join(inputs_san)}]"
            lines.append(f"  {output} <> {expr};")
        elif gate_type == 'xor':
            # XOR: synthesize recursively
            if len(inputs_san) == 1:
                lines.append(f"  {output} <> {inputs_san[0]};")
            elif len(inputs_san) == 2:
                expr = f"OR <> [AND <> [{inputs_san[0]}, NOT <> {inputs_san[1]}], AND <> [NOT <> {inputs_san[0]}, {inputs_san[1]}]]"
                lines.append(f"  {output} <> {expr};")
            else:
                # Multi-input XOR: chain pairwise
                acc = inputs_san[0]
                for i in range(1, len(inputs_san)):
                    acc = f"OR <> [AND <> [{acc}, NOT <> {inputs_san[i]}], AND <> [NOT <> {acc}, {inputs_san[i]}]]"
                lines.append(f"  {output} <> {acc};")
        elif gate_type == 'xnor':
            # XNOR: NOT(XOR(inputs))
            if len(inputs_san) == 1:
                lines.append(f"  {output} <> NOT <> {inputs_san[0]};")
            elif len(inputs_san) == 2:
                expr = f"NOT <> OR <> [AND <> [{inputs_san[0]}, NOT <> {inputs_san[1]}], AND <> [NOT <> {inputs_san[0]}, {inputs_san[1]}]]"
                lines.append(f"  {output} <> {expr};")
            else:
                # Multi-input XNOR
                acc = inputs_san[0]
                for i in range(1, len(inputs_san)):
                    acc = f"OR <> [AND <> [{acc}, NOT <> {inputs_san[i]}], AND <> [NOT <> {acc}, {inputs_san[i]}]]"
                lines.append(f"  {output} <> NOT <> {acc};")

    lines.append("}")
    return '\n'.join(lines) + '\n'

# -----------------------------
# CLI
# -----------------------------

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('input', help='Input Verilog .v netlist')
    ap.add_argument('-o', '--output', help='Output .nx file')
    args = ap.parse_args()

    with open(args.input, 'r') as f:
        text = f.read()

    mod_name, ports, inputs, outputs, wires, assigns, gates = extract_module(text)
    nx = generate_nx(mod_name, ports, inputs, outputs, wires, assigns, gates)

    out_path = args.output
    if not out_path:
        base = os.path.splitext(os.path.basename(args.input))[0]
        out_path = os.path.join(os.path.dirname(args.input), base + '.nx')
    with open(out_path, 'w') as f:
        f.write(nx)
    print(f"Wrote NX to {out_path}")

if __name__ == '__main__':
    main()
