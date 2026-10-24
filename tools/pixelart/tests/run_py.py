#!/usr/bin/env python3
"""Python-side golden test runner.

Compares the Python DSL result (dsl.py) for every tests/cases/*.pxs against
its *.expected.json (full image metadata + pixels + warnings). Generated via
`python3 tests/gen_expected.py`.
"""
import json
import sys
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

import dsl  # noqa: E402


def main():
    cases_dir = BASE / "tests" / "cases"
    cases = sorted(cases_dir.glob("*.pxs"))
    if not cases:
        print("no cases found")
        return 1
    failed = 0
    for case in cases:
        expected = json.loads(case.with_suffix(".expected.json").read_text(encoding="utf-8"))
        try:
            img, warnings = dsl.run(case.read_text(encoding="utf-8"), require_canvas=True)
            got = {
                "name": img["name"],
                "width": img["width"],
                "height": img["height"],
                "frameWidth": img["frameWidth"],
                "frameHeight": img["frameHeight"],
                "pixels": img["pixels"],
                "warnings": warnings,
            }
        except Exception as e:  # PxsError or unexpected
            print(f"FAIL {case.name}: raised {e}")
            failed += 1
            continue
        if got == expected:
            print(f"pass {case.name}")
        else:
            print(f"FAIL {case.name}")
            for key in ("name", "width", "height", "frameWidth", "frameHeight", "pixels", "warnings"):
                if got[key] != expected[key]:
                    print(f"  field {key} differs")
            failed += 1
    print(f"total: {len(cases)}, failed: {failed}")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
