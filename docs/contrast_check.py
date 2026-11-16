#!/usr/bin/env python3
"""
docs/contrast_check.py
Implements WCAG 2 relative-luminance contrast ratio calculation using only
the Python 3 standard library.
"""

import sys

def srgb_to_linear(val_byte):
    s = val_byte / 255.0
    if s <= 0.03928:
        return s / 12.92
    else:
        return ((s + 0.055) / 1.055) ** 2.4

def relative_luminance(hex_str):
    hex_clean = hex_str.strip().lstrip('#')
    r = int(hex_clean[0:2], 16)
    g = int(hex_clean[2:4], 16)
    b = int(hex_clean[4:6], 16)
    r_lin = srgb_to_linear(r)
    g_lin = srgb_to_linear(g)
    b_lin = srgb_to_linear(b)
    return 0.2126 * r_lin + 0.7152 * g_lin + 0.0722 * b_lin

def contrast_ratio(hex1, hex2):
    l1 = relative_luminance(hex1)
    l2 = relative_luminance(hex2)
    lighter = max(l1, l2)
    darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)

REQUIRED_TEXT_PAIRS = [
    ("main on white", "#263635", "#FFFFFF", 4.5),
    ("main on off-white", "#263635", "#F8FAF7", 4.5),
    ("main on pale mint", "#263635", "#E4F0E9", 4.5),
    ("main on warm mint", "#263635", "#A8D5C8", 4.5),
    ("secondary on white", "#5A6662", "#FFFFFF", 4.5),
    ("secondary on off-white", "#5A6662", "#F8FAF7", 4.5),
    ("secondary on pale mint", "#5A6662", "#E4F0E9", 4.5),
    ("white on deep teal", "#FFFFFF", "#164E50", 4.5),
    ("white on ocean teal", "#FFFFFF", "#267878", 4.5),
    ("deep teal on white", "#164E50", "#FFFFFF", 4.5),
    ("deep teal on off-white", "#164E50", "#F8FAF7", 4.5),
    ("deep teal on pale mint", "#164E50", "#E4F0E9", 4.5),
    ("deep teal on warm mint", "#164E50", "#A8D5C8", 4.5),
    ("ocean teal on white", "#267878", "#FFFFFF", 4.5),
    ("ocean teal on off-white", "#267878", "#F8FAF7", 4.5),
    ("green text on white", "#2F6F5A", "#FFFFFF", 4.5),
    ("green text on off-white", "#2F6F5A", "#F8FAF7", 4.5),
    ("green text on pale mint", "#2F6F5A", "#E4F0E9", 4.5),
    ("amber text on white", "#8F5E12", "#FFFFFF", 4.5),
    ("amber text on off-white", "#8F5E12", "#F8FAF7", 4.5),
    ("amber text on pale mint", "#8F5E12", "#E4F0E9", 4.5),
    ("grey text on white", "#666A72", "#FFFFFF", 4.5),
    ("grey text on off-white", "#666A72", "#F8FAF7", 4.5),
    ("grey text on pale mint", "#666A72", "#E4F0E9", 4.5),
    ("red text on white", "#AD464E", "#FFFFFF", 4.5),
    ("red text on off-white", "#AD464E", "#F8FAF7", 4.5),
    ("red text on pale mint", "#AD464E", "#E4F0E9", 4.5),
    ("badge tint: green on tint", "#2F6F5A", "#E6F2EE", 4.5),
    ("badge tint: amber on tint", "#8F5E12", "#FBF1E0", 4.5),
    ("badge tint: grey on tint", "#666A72", "#ECEDEF", 4.5),
    ("badge tint: red on tint", "#AD464E", "#F8E8E9", 4.5),
    ("white on destructive red", "#FFFFFF", "#AD464E", 4.5),
    ("main text on amber fill", "#263635", "#D69A42", 4.5),
]

REQUIRED_NON_TEXT_PAIRS = [
    ("ocean teal on white (UI/icon)", "#267878", "#FFFFFF", 3.0),
    ("ocean teal on off-white (UI/icon)", "#267878", "#F8FAF7", 3.0),
    ("ocean teal on pale mint (UI/icon)", "#267878", "#E4F0E9", 3.0),
    ("deep teal on white (UI/icon)", "#164E50", "#FFFFFF", 3.0),
    ("deep teal on off-white (UI/icon)", "#164E50", "#F8FAF7", 3.0),
    ("deep teal on pale mint (UI/icon)", "#164E50", "#E4F0E9", 3.0),
    ("outline on white (boundary)", "#75817D", "#FFFFFF", 3.0),
    ("outline on off-white (boundary)", "#75817D", "#F8FAF7", 3.0),
    ("outline on pale mint (boundary)", "#75817D", "#E4F0E9", 3.0),
]

DECORATION_ONLY_PAIRS = [
    ("soft turquoise on white", "#55A6A0", "#FFFFFF", 3.0),
    ("soft turquoise on off-white", "#55A6A0", "#F8FAF7", 3.0),
    ("soft turquoise on pale mint", "#55A6A0", "#E4F0E9", 3.0),
    ("amber fill on white", "#D69A42", "#FFFFFF", 3.0),
    ("grey fill on white", "#898D95", "#FFFFFF", 3.0),
    ("old muted text on white", "#75817D", "#FFFFFF", 4.5),
]

def main():
    print("=== REQUIRED TEXT PAIRS (min 4.5:1) ===")
    required_failed = 0
    for name, fg, bg, min_req in REQUIRED_TEXT_PAIRS:
        ratio = contrast_ratio(fg, bg)
        passed = ratio >= min_req
        status = "PASS" if passed else "FAIL"
        if not passed:
            required_failed += 1
        print(f"{name:<35} | {fg} on {bg} | {ratio:5.2f}:1 (min {min_req:3.1f}) | {status}")

    print("\n=== REQUIRED NON-TEXT PAIRS (min 3.0:1) ===")
    for name, fg, bg, min_req in REQUIRED_NON_TEXT_PAIRS:
        ratio = contrast_ratio(fg, bg)
        passed = ratio >= min_req
        status = "PASS" if passed else "FAIL"
        if not passed:
            required_failed += 1
        print(f"{name:<35} | {fg} on {bg} | {ratio:5.2f}:1 (min {min_req:3.1f}) | {status}")

    print("\n=== DECORATION-ONLY PAIRS (Expected Failures) ===")
    for name, fg, bg, min_req in DECORATION_ONLY_PAIRS:
        ratio = contrast_ratio(fg, bg)
        failed_as_expected = ratio < min_req
        if failed_as_expected:
            status = "EXPECTED FAIL (do not use for text or icons)"
        else:
            status = "WARNING: UNEXPECTED PASS"
        print(f"{name:<35} | {fg} on {bg} | {ratio:5.2f}:1 (min {min_req:3.1f}) | {status}")

    print("\n-------------------------------------------------------------")
    if required_failed > 0:
        print(f"FAILED: {required_failed} required pair(s) failed contrast threshold.")
        sys.exit(1)
    else:
        print("ALL REQUIRED PAIRS PASSED.")
        sys.exit(0)

if __name__ == "__main__":
    main()
