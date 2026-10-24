#!/usr/bin/env python3
"""Regenerate *.expected.json golden files from the *.pxs cases.

Run:  python3 tests/gen_expected.py
Only run this when the interpreter behavior was deliberately changed.
"""
import json
import sys
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

import dsl  # noqa: E402


def main():
    cases = sorted((BASE / "tests" / "cases").glob("*.pxs"))
    for case in cases:
        img, warnings = dsl.run(case.read_text(encoding="utf-8"), require_canvas=True)
        expected = {
            "name": img["name"],
            "width": img["width"],
            "height": img["height"],
            "frameWidth": img["frameWidth"],
            "frameHeight": img["frameHeight"],
            "pixels": img["pixels"],
            "warnings": warnings,
        }
        out = case.with_suffix(".expected.json")
        out.write_text(json.dumps(expected, indent=2) + "\n", encoding="utf-8")
        print(f"generated: {out.name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
