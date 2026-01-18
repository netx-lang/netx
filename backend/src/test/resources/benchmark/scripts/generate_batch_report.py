#!/usr/bin/env python3
"""
Generate batch PPA comparison report for NetX optimization results.
Reads data directly from area and timing log files and generates a formatted table.
"""

import os
import sys
import re
from datetime import datetime

def extract_cells_from_area_file(area_file):
    """Extract number of cells from area.txt file."""
    if not os.path.exists(area_file):
        return None
    
    with open(area_file, 'r') as f:
        for line in f:
            if 'Number of cells:' in line:
                match = re.search(r'Number of cells:\s+(\d+)', line)
                if match:
                    return match.group(1)
    return None

def extract_timing_from_timing_file(timing_file):
    """Extract critical path timing from timing.txt file."""
    if not os.path.exists(timing_file):
        return None
    
    with open(timing_file, 'r') as f:
        for line in f:
            if 'data arrival time' in line and line.strip().endswith('data arrival time'):
                parts = line.split()
                if len(parts) >= 1:
                    try:
                        return f"{float(parts[0]):.3f}"
                    except ValueError:
                        pass
    return None

def extract_metrics_from_design(design_dir, design_name):
    """Extract metrics from a single design's log files."""
    area_file = os.path.join(design_dir, f"{design_name}_area.txt")
    timing_file = os.path.join(design_dir, f"{design_name}_timing.txt")
    
    cells = extract_cells_from_area_file(area_file)
    timing = extract_timing_from_timing_file(timing_file)
    
    return cells, timing

def collect_all_results(build_dir):
    """Collect results from all design directories in build folder."""
    results = []
    
    if not os.path.exists(build_dir):
        return results
    
    for design_name in sorted(os.listdir(build_dir)):
        design_path = os.path.join(build_dir, design_name)
        
        if not os.path.isdir(design_path):
            continue
        
        origin_dir = os.path.join(design_path, f"{design_name}_origin")
        opted_dir = os.path.join(design_path, f"{design_name}_opted")
        
        if not os.path.isdir(origin_dir) or not os.path.isdir(opted_dir):
            continue
        
        origin_cells, origin_time = extract_metrics_from_design(origin_dir, f"{design_name}_origin")
        opted_cells, opted_time = extract_metrics_from_design(opted_dir, f"{design_name}_opted")
        
        if origin_cells and opted_cells and origin_time and opted_time:
            try:
                origin_cells_int = int(origin_cells)
                opted_cells_int = int(opted_cells)
                origin_time_float = float(origin_time)
                opted_time_float = float(opted_time)
                
                cell_delta = ((opted_cells_int - origin_cells_int) / origin_cells_int * 100) if origin_cells_int > 0 else 0
                time_delta = ((opted_time_float - origin_time_float) / origin_time_float * 100) if origin_time_float > 0 else 0
                
                results.append({
                    'design': design_name,
                    'origin_cells': origin_cells,
                    'origin_time': origin_time,
                    'opted_cells': opted_cells,
                    'opted_time': opted_time,
                    'cell_delta': f"{cell_delta:.1f}%",
                    'time_delta': f"{time_delta:.1f}%"
                })
            except (ValueError, ZeroDivisionError):
                pass
    
    return results

def generate_table(results, output_file=None):
    """Generate formatted table from results."""
    lines = []
    
    lines.append("=" * 105)
    lines.append(f"{'Design':<20} {'Origin_Cells':>12} {'Origin_Time':>12} {'Opted_Cells':>12} {'Opted_Time':>12} {'Cell_Δ':>10} {'Time_Δ':>10}")
    lines.append("=" * 105)
    
    for result in results:
        lines.append(
            f"{result['design']:<20} "
            f"{result['origin_cells']:>12} "
            f"{result['origin_time']:>12} "
            f"{result['opted_cells']:>12} "
            f"{result['opted_time']:>12} "
            f"{result['cell_delta']:>10} "
            f"{result['time_delta']:>10}"
        )
    
    lines.append("=" * 105)
    
    output_text = '\n'.join(lines)
    
    if output_file:
        with open(output_file, 'w') as f:
            f.write(output_text + '\n')
    
    return output_text

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    workspace_root = os.path.dirname(script_dir)
    build_dir = os.path.join(workspace_root, 'build')
    output_file = os.path.join(build_dir, 'summary.txt')
    
    if len(sys.argv) > 1:
        build_dir = sys.argv[1]
    if len(sys.argv) > 2:
        output_file = sys.argv[2]
    
    results = collect_all_results(build_dir)
    
    if not results:
        print("Error: No comparison results found in build directory", file=sys.stderr)
        sys.exit(1)
    
    table = generate_table(results, output_file)
    print(table)
    
    if output_file:
        print(f"\nSummary saved to: {output_file}")

if __name__ == '__main__':
    main()
