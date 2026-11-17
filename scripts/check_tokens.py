#!/usr/bin/env python3
"""
scripts/check_tokens.py: Hard-coding check for ShieldHer (FE-3.1d).

Checks:
1. Every layout XML under app/src/main/res/layout* NOT in scripts/token_check_stage1_layouts.txt for:
   - Hex color literals (#RGB, #RRGGBB, #AARRGGBB)
   - android:text, android:hint, android:contentDescription, android:title, app:title not starting with "@"
     (tools:* attributes are ignored)
   - Dimension literals in dp or sp other than 0dp (e.g. 8dp, 14sp, match_parent/wrap_content/0dp are fine)
   - Drawables and values files are not checked here.
2. Kotlin files under app/src/main/java/com/shieldher/dashboard for:
   - Color.parseColor(, Color.rgb(, Color.argb(
3. Excludes:
   - app/src/debug, app/src/test, app/src/androidTest
   - styles.xml, themes.xml, dimens.xml, colors.xml
"""

import sys
import os
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

EXCLUSION_FILE = REPO_ROOT / "scripts" / "token_check_stage1_layouts.txt"
LAYOUTS_DIR = REPO_ROOT / "app" / "src" / "main" / "res"
DASHBOARD_KT_DIR = REPO_ROOT / "app" / "src" / "main" / "java" / "com" / "shieldher" / "dashboard"

# Regex patterns
HEX_COLOR_RE = re.compile(r'#[0-9a-fA-F]{3,8}\b')
ATTR_TEXT_RE = re.compile(r'(android:(?:text|hint|contentDescription|title)|app:title)\s*=\s*"([^"]*)"')
DIMEN_LITERAL_RE = re.compile(r'(android:\w+|app:\w+)\s*=\s*"([^"]*)"')
DP_SP_VAL_RE = re.compile(r'^-?\d+(?:\.\d+)?(dp|sp)$')
KT_COLOR_RE = re.compile(r'\bColor\.(parseColor|rgb|argb)\s*\(')

def load_excluded_layouts():
    if not EXCLUSION_FILE.exists():
        return set()
    with open(EXCLUSION_FILE, "r", encoding="utf-8") as f:
        return {line.strip() for line in f if line.strip() and not line.startswith("#")}

def check_layout_file(filepath: Path, rel_path: str):
    violations = []
    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()

    for idx, line in enumerate(lines, start=1):
        # Ignore comments or lines inside XML comments if any
        stripped = line.strip()
        if stripped.startswith("<!--") or stripped.startswith("-->"):
            continue

        # 1. Hex color literals
        hex_matches = HEX_COLOR_RE.findall(line)
        for h in hex_matches:
            violations.append((rel_path, idx, "hardcoded-hex-color", f"Hex color literal found: {h}"))

        # 2. Text / hint / contentDescription / title attributes
        for attr, val in ATTR_TEXT_RE.findall(line):
            if val and not val.startswith("@"):
                violations.append((rel_path, idx, "hardcoded-string", f'{attr} has hardcoded text: "{val}"'))

        # 3. Dimension literals in dp or sp other than 0dp
        for attr, val in DIMEN_LITERAL_RE.findall(line):
            if attr.startswith("tools:"):
                continue
            m = DP_SP_VAL_RE.match(val.strip())
            if m:
                if val.strip() == "0dp":
                    continue
                violations.append((rel_path, idx, "hardcoded-dimen", f'{attr} has literal dimension: "{val}"'))

    return violations

def check_kotlin_file(filepath: Path, rel_path: str):
    violations = []
    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()

    for idx, line in enumerate(lines, start=1):
        if KT_COLOR_RE.search(line):
            violations.append((rel_path, idx, "kotlin-color-call", f"Direct Color creation/parsing call: {line.strip()}"))

    return violations

def main():
    excluded_layouts = load_excluded_layouts()
    all_violations = []
    checked_layouts = 0
    checked_kt = 0

    # 1. Scan layouts
    for root, dirs, files in os.walk(LAYOUTS_DIR):
        dir_name = os.path.basename(root)
        if not dir_name.startswith("layout"):
            continue
        for fname in files:
            if not fname.endswith(".xml"):
                continue
            if fname in excluded_layouts:
                continue
            layout_path = Path(root) / fname
            rel_path = os.path.relpath(layout_path, REPO_ROOT)
            checked_layouts += 1
            all_violations.extend(check_layout_file(layout_path, rel_path))

    # 2. Scan Kotlin files in dashboard
    if DASHBOARD_KT_DIR.exists():
        for root, dirs, files in os.walk(DASHBOARD_KT_DIR):
            for fname in files:
                if not fname.endswith(".kt"):
                    continue
                kt_path = Path(root) / fname
                rel_path = os.path.relpath(kt_path, REPO_ROOT)
                checked_kt += 1
                all_violations.extend(check_kotlin_file(kt_path, rel_path))

    # Report results
    print(f"Token check scanned {checked_layouts} dashboard layout(s) and {checked_kt} dashboard Kotlin file(s).")
    print(f"Stage 1 excluded layouts: {len(excluded_layouts)}")

    if all_violations:
        print(f"\nFound {len(all_violations)} violation(s):")
        for file_path, line_no, rule, msg in all_violations:
            print(f"{file_path}:{line_no}: [{rule}] {msg}")
        sys.exit(1)
    else:
        print("\nAll token and hardcoding checks PASSED.")
        sys.exit(0)

if __name__ == "__main__":
    main()
