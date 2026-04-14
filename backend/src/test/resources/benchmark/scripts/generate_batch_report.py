#!/usr/bin/env python3
"""
Generate batch PPA comparison report for NetX optimization results.
Reads data directly from area and timing log files and generates a formatted table.
"""

import os
import sys
import re
import math
from datetime import datetime

def read_benchmark_categories(conf_file):
    """Read benchmark.conf and return a dict mapping design names to categories."""
    categories = {}
    if not os.path.exists(conf_file):
        return categories
    
    with open(conf_file, 'r') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            if '=' in line:
                design, category = line.split('=', 1)
                categories[design.strip()] = category.strip()
    
    return categories

def extract_chip_area_from_area_file(area_file):
    """Extract chip area in µm^2 from area.txt file."""
    if not os.path.exists(area_file):
        return None

    with open(area_file, 'r') as f:
        for line in f:
            match = re.search(r"Chip area for module '\\[^']+':\s+([0-9]+\.[0-9]+)", line)
            if match:
                try:
                    return float(match.group(1))
                except ValueError:
                    return None
    return None

def extract_timing_from_log_file(log_file):
    """Extract critical path timing from OpenROAD log file."""
    if not os.path.exists(log_file):
        return None
    
    with open(log_file, 'r') as f:
        # Read all lines and find the last occurrence of "data arrival time"
        # which appears in the OpenROAD timing report section
        last_timing = None
        for line in f:
            # Look for lines like "           905.9808   data arrival time"
            if 'data arrival time' in line:
                parts = line.split()
                if len(parts) >= 1:
                    try:
                        timing_value = float(parts[0])
                        # Only consider non-zero positive values
                        if timing_value > 0:
                            last_timing = f"{timing_value:.3f}"
                    except ValueError:
                        pass
        return last_timing

def extract_metrics_from_design(design_dir, design_name):
    """Extract metrics from a single design's log files."""
    area_file = os.path.join(design_dir, f"{design_name}_area.txt")
    or_log_file = os.path.join(design_dir, f"{design_name}_or.log")

    area = extract_chip_area_from_area_file(area_file)
    timing = extract_timing_from_log_file(or_log_file)

    if area is None or timing is None:
        return None

    return {
        'area': area,
        'time': float(timing),
    }

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
        
        origin_metrics = extract_metrics_from_design(origin_dir, f"{design_name}_origin")
        opted_metrics = extract_metrics_from_design(opted_dir, f"{design_name}_opted")

        if origin_metrics and opted_metrics:
            try:
                origin_area_float = float(origin_metrics['area'])
                opted_area_float = float(opted_metrics['area'])
                origin_time_float = float(origin_metrics['time'])
                opted_time_float = float(opted_metrics['time'])
                
                area_delta = ((opted_area_float - origin_area_float) / origin_area_float * 100) if origin_area_float > 0 else 0
                time_delta = ((opted_time_float - origin_time_float) / origin_time_float * 100) if origin_time_float > 0 else 0
                
                results.append({
                    'design': design_name,
                    'origin_area': f"{origin_area_float:.3f}",
                    'origin_time': f"{origin_time_float:.3f}",
                    'opted_area': f"{opted_area_float:.3f}",
                    'opted_time': f"{opted_time_float:.3f}",
                    'area_delta': f"{area_delta:.1f}%",
                    'time_delta': f"{time_delta:.1f}%",
                    'area_delta_raw': area_delta,
                    'time_delta_raw': time_delta,
                })
            except (ValueError, ZeroDivisionError):
                pass
    
    return results

def compute_geomean(values):
    """Compute geometric mean of a list of values."""
    if not values:
        return 1.0
    
    product = 1.0
    for v in values:
        product *= v
    
    geomean = product ** (1.0 / len(values))
    
    return geomean

def generate_table_for_category(results, category_name):
    """Generate formatted table for a specific category."""
    lines = []
    
    lines.append("")
    length = len(category_name) + 2
    lines.append(f"{'='*(50 - length//2)} {category_name} {'='*(100 - 50 + length//2 - length)}")
    lines.append(
        f"{'Design':<20} "
        f"{'Origin_Area':>12} {'Origin_Time':>12} "
        f"{'Opted_Area':>12} {'Opted_Time':>12} "
        f"{'Area_Δ':>10} {'Time_Δ':>10}"
    )
    lines.append("=" * 100)
    
    # Collect ratios for geomean calculation (more meaningful than averaging absolutes)
    area_ratios = []
    time_ratios = []

    # Collect absolute differences (opted - origin)
    area_diff_sum = 0.0
    time_diff_sum = 0.0
    diff_count = 0
    
    for result in results:
        lines.append(
            f"{result['design']:<20} "
            f"{result['origin_area']:>12} "
            f"{result['origin_time']:>12} "
            f"{result['opted_area']:>12} "
            f"{result['opted_time']:>12} "
            f"{result['area_delta']:>10} "
            f"{result['time_delta']:>10}"
        )
        try:
            origin_area = float(result['origin_area'])
            opted_area = float(result['opted_area'])
            origin_time = float(result['origin_time'])
            opted_time = float(result['opted_time'])

            area_diff_sum += (opted_area - origin_area)
            time_diff_sum += (opted_time - origin_time)
            diff_count += 1

            if origin_area > 0:
                area_ratios.append(opted_area / origin_area)
            if origin_time > 0:
                time_ratios.append(opted_time / origin_time)
        except (ValueError, ZeroDivisionError):
            pass

    # Report sum of absolute numeric differences (opted - origin)
    if diff_count > 0:
        lines.append("=" * 100)
        lines.append(
            f"{'ABS_Δ':<20} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{area_diff_sum:>10.3f} "
            f"{time_diff_sum:>10.3f}"
        )
    
    # Compute geometric means of ratios and report as percent deltas
    if area_ratios or time_ratios:
        area_ratio_geomean = compute_geomean(area_ratios) if area_ratios else 1.0
        time_ratio_geomean = compute_geomean(time_ratios) if time_ratios else 1.0

        area_delta_geomean = (area_ratio_geomean - 1.0) * 100
        time_delta_geomean = (time_ratio_geomean - 1.0) * 100
        
        lines.append(
            f"{'GEOMEAN':<20} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{'-':>12} "
            f"{area_delta_geomean:>9.1f}% "
            f"{time_delta_geomean:>9.1f}%"
        )
        lines.append("=" * 100)
    
    return lines

def generate_table(results, categories, output_file=None):
    """Generate formatted table from results, split by categories."""
    lines = []
    
    # Group results by category
    results_by_category = {}
    uncategorized = []
    
    for result in results:
        design = result['design']
        if design in categories:
            category = categories[design]
            if category not in results_by_category:
                results_by_category[category] = []
            results_by_category[category].append(result)
        else:
            uncategorized.append(result)
    
    # Generate table for each category
    for category in sorted(results_by_category.keys()):
        lines.extend(generate_table_for_category(results_by_category[category], category))
    
    # Add uncategorized if any
    if uncategorized:
        lines.extend(generate_table_for_category(uncategorized, "UNCATEGORIZED"))
    
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
    conf_file = os.path.join(script_dir, 'benchmark.conf')
    
    if len(sys.argv) > 1:
        build_dir = sys.argv[1]
    if len(sys.argv) > 2:
        output_file = sys.argv[2]
    
    # Read benchmark categories
    categories = read_benchmark_categories(conf_file)
    
    results = collect_all_results(build_dir)
    
    if not results:
        print("Error: No comparison results found in build directory", file=sys.stderr)
        sys.exit(1)
    
    table = generate_table(results, categories, output_file)
    print(table)
    
    if output_file:
        print(f"\nSummary saved to: {output_file}")

if __name__ == '__main__':
    main()
