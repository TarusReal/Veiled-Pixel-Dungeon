#!/usr/bin/env python3
"""CLI for Veiled Pixel Dungeon pixel-art assets."""

import argparse
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(BASE_DIR))

from pixelart import (
    new_image, load_json, save_json, render, import_png,
    unique_colors, flip_h, flip_v, rotate_90, scale_nearest,
)
from presets import PRESETS, preset_names, get_preset
from validate import validate_path, validate_json
from promote import promote

DEFAULT_STAGING = Path("/tmp/opencode/pixelart-staging")


def _staging_dir(args):
    return Path(getattr(args, "staging", None) or DEFAULT_STAGING)


def cmd_new(args):
    preset = None
    if args.preset:
        preset = get_preset(args.preset)
    width = args.width if args.width else (preset["width"] if preset else 16)
    height = args.height if args.height else (preset["height"] if preset else 16)
    fw = preset["frameWidth"] if preset else width
    fh = preset["frameHeight"] if preset else height
    mode = preset["mode"] if preset else "rgba"
    img = new_image(name=args.name, width=width, height=height, mode=mode,
                    frame_width=fw, frame_height=fh)
    out = Path(args.out) if args.out else (_staging_dir(args) / (args.name + ".px.json"))
    out.parent.mkdir(parents=True, exist_ok=True)
    save_json(img, out)
    print(f"created {out}")
    return 0


def cmd_render(args):
    img = load_json(args.src)
    out = Path(args.out) if args.out else Path(args.src).with_suffix(".png")
    # if src is .px.json, .with_suffix gives .px.png? handle
    if out.suffix != ".png":
        out = out.with_suffix(".png")
    render(img, out)
    print(f"rendered {out}")
    return 0


def cmd_import(args):
    img = import_png(args.src, name=args.name)
    if args.preset:
        p = get_preset(args.preset)
        img["frameWidth"] = p["frameWidth"]
        img["frameHeight"] = p["frameHeight"]
    out = Path(args.out) if args.out else Path(args.src).with_suffix(".px.json")
    save_json(img, out)
    print(f"imported {out}")
    return 0


def cmd_validate(args):
    issues = validate_path(args.path, asset_type=args.type, assets_java_path=args.assets_java)
    hard = [i for i in issues if not i.startswith("WARNING:") and not i.startswith("NOTE:")]
    if hard:
        print(f"INVALID: {args.path}")
        for i in issues:
            print("  - " + i)
        return 1
    if issues:
        print(f"OK (with warnings): {args.path}")
        for i in issues:
            print("  - " + i)
    else:
        print(f"OK: {args.path}")
    return 0


def cmd_promote(args):
    ok, msgs = promote(
        args.src,
        target_subdir=args.target,
        asset_type=args.type,
        preset=args.preset,
        force=args.force,
        assets_root=Path(args.assets_root),
        assets_java=Path(args.assets_java),
    )
    for m in msgs:
        print(m)
    return 0 if ok else 1


def cmd_palette(args):
    colors = unique_colors(args.src)
    if args.format == "json":
        print(json.dumps(colors))
    else:
        for c in colors:
            print("#%02x%02x%02x%02x  %s" % (c[0], c[1], c[2], c[3], c))
    print(f"total: {len(colors)}", file=sys.stderr)
    return 0


def cmd_variants(args):
    base = load_json(args.base)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    n = args.count
    for i in range(1, n + 1):
        import copy
        v = copy.deepcopy(base)
        v["name"] = f"{base['name']}_v{i}"
        save_json(v, out_dir / f"{base['name']}_v{i}.px.json")
        render(v, out_dir / f"{base['name']}_v{i}.png")
    print(f"created {n} variants in {out_dir}")
    return 0


def cmd_info(args):
    print("Presets:")
    for name in preset_names():
        p = PRESETS[name]
        print(f"  {name:16s} {p['width']:4d}x{p['height']:<4d} frame {p['frameWidth']}x{p['frameHeight']}  -> {p['target_dir']}  ({p['desc']})")
    print()
    print("Default staging dir:", DEFAULT_STAGING)
    print("Assets root: core/src/main/assets")
    return 0


def cmd_list(args):
    d = _staging_dir(args)
    if not d.exists():
        print(f"staging dir not found: {d}")
        return 0
    for p in sorted(d.iterdir()):
        print(p.name)
    return 0


# ---------------------------------------------------------------------------
# pxs DSL commands (compile / draw / preview / selftest)
# ---------------------------------------------------------------------------

RAMP = "@%#*+=-:. "  # dark -> light


def _print_preview(img, width=0):
    w, h = img["width"], img["height"]
    # ruler with frame boundaries
    if img.get("frameWidth", w) != w and img.get("frameWidth"):
        ruler = ""
        for x in range(0, w, img["frameWidth"]):
            ruler += "|" + " " * (img["frameWidth"] - 1 if x > 0 else img["frameWidth"] - 1)
        print(ruler[: w])
    out_rows = []
    for y in range(h):
        row = ""
        for x in range(w):
            px = img["pixels"][y * w + x]
            if px[3] == 0:
                row += "."
            else:
                lum = (0.299 * px[0] + 0.587 * px[1] + 0.114 * px[2]) / 255.0
                row += RAMP[min(len(RAMP) - 1, int(lum * (len(RAMP) - 1)))]
        if width and width > 0:
            for k in range(0, w, width):
                out_rows.append(row[k:k + width])
        else:
            out_rows.append(row)
    print("\n".join(out_rows))


def _has_canvas_cmd(text):
    for raw in text.splitlines():
        line = _strip_comment(raw) if "#" in raw else raw.strip()
        if not line:
            continue
        toks = line.split()
        if toks[0] == "canvas":
            return True
    return False


def _strip_comment(raw):
    return raw.split("#", 1)[0].strip()


def _run_script(text, img=None, require_canvas=False):
    import dsl
    tree = dsl.parse(text)
    script = dsl.Script(tree)
    out_img = script.run(img, require_canvas=require_canvas)
    return out_img, script.warnings


def _finish(common, img, warnings, dry_run, preview, out_path=None, png_path=None, recipe=None):
    if recipe is not None:
        img["recipe"] = recipe
    if preview:
        _print_preview(img)
    for w in warnings:
        print(f"WARNING: {w}")
    if dry_run:
        print("dry-run OK")
        return 0
    if out_path is not None:
        save_json(img, out_path)
        print(f"saved: {out_path}")
    if png_path is not None:
        import pixelart as P
        P.render(img, png_path)
        print(f"rendered: {png_path}")
    return 0


def cmd_compile(args):
    import dsl
    script_path = Path(args.script)
    if not script_path.exists():
        print(f"script not found: {script_path}")
        return 1
    text = script_path.read_text(encoding="utf-8")
    try:
        img, warnings = _run_script(text, require_canvas=True)
    except dsl.PxsError as e:
        print(f"ERROR: {e}")
        return 1
    if img["name"] == "untitled":
        img["name"] = script_path.stem
    out = Path(args.out) if args.out else script_path.with_name(script_path.stem + ".px.json")
    if out.suffixes[-2:] != [".px", ".json"]:
        out = out.with_name(out.stem + ".px.json")
    png = Path(args.png) if args.png else None
    return _finish(args, img, warnings, args.dry_run, args.preview,
                   out_path=None if args.dry_run else out,
                   png_path=(None if (args.dry_run or not png) else png),
                   recipe=text)


def cmd_draw(args):
    import dsl
    target = Path(args.target)
    cmds_text = "\n".join(args.commands)
    if not args.commands:
        print("no commands given")
        return 1
    exists = target.exists()
    img = None
    warnings = []
    if exists and target.suffix == ".json":
        data = load_json(target)
        prior_recipe = data.get("recipe")
        try:
            if prior_recipe:
                recipe = prior_recipe + "\n" + cmds_text
            else:
                if _has_canvas_cmd(cmds_text):
                    recipe = cmds_text
                else:
                    recipe = f"canvas {data['width']} {data['height']}\n" + cmds_text
            img, warnings = _run_script(recipe, img=data)
        except dsl.PxsError as e:
            print(f"ERROR: {e}")
            return 1
        if img["name"] == "untitled":
            img["name"] = target.stem.replace(".px", "")
        recipe_full = recipe
    else:
        if exists and target.suffix != ".json":
            print(f"unsupported target type: {target.suffix}")
            return 1
        try:
            img, warnings = _run_script(cmds_text, require_canvas=True)
        except dsl.PxsError as e:
            print(f"ERROR: {e}")
            return 1
        if img["name"] == "untitled":
            img["name"] = target.stem.replace(".px", "")
        recipe_full = cmds_text
    if args.preview:
        _print_preview(img)
    for w in warnings:
        print(f"WARNING: {w}")
    if args.dry_run:
        print("dry-run OK")
        return 0
    save_json(img, target, {"recipe": recipe_full})
    print(f"saved: {target}")
    png = Path(args.png) if args.png else None
    if png:
        import pixelart as P
        P.render(img, png)
        print(f"rendered: {png}")
    return 0


def cmd_preview(args):
    src = Path(args.src)
    if src.suffix == ".pxs":
        text = src.read_text(encoding="utf-8")
        try:
            img, warnings = _run_script(text, require_canvas=True)
        except Exception as e:
            print(f"ERROR: {e}")
            return 1
        _print_preview(img, width=args.width)
        for w in warnings:
            print(f"WARNING: {w}")
        return 0
    if src.suffix == ".json":
        img = load_json(src)
        _print_preview(img, width=args.width)
        return 0
    if src.suffix.lower() == ".png":
        import pixelart as P
        img = P.import_png(src)
        _print_preview(img, width=args.width)
        return 0
    print(f"unsupported file type: {src.suffix}")
    return 1


def cmd_selftest(args):
    runner = BASE_DIR / "tests" / "run_py.py"
    if not runner.exists():
        print(f"no runner: {runner}")
        return 1
    proc = subprocess.run(
        [sys.executable, str(runner)],
        capture_output=True, text=True,
    )
    sys.stdout.write(proc.stdout)
    if proc.stderr:
        sys.stderr.write(proc.stderr)
    return proc.returncode


def build_parser():
    parser = argparse.ArgumentParser(prog="px.py", description="Veiled Pixel Dungeon pixel-art helper")
    parser.add_argument("--staging", default=None, help="override staging dir")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("new", help="create a blank .px.json")
    p.add_argument("name")
    p.add_argument("--preset", default=None)
    p.add_argument("--width", type=int, default=None)
    p.add_argument("--height", type=int, default=None)
    p.add_argument("--out", default=None)
    p.set_defaults(func=cmd_new)

    p = sub.add_parser("render", help="render .px.json -> .png")
    p.add_argument("src")
    p.add_argument("--out", default=None)
    p.set_defaults(func=cmd_render)

    p = sub.add_parser("import", help="import .png -> .px.json")
    p.add_argument("src")
    p.add_argument("--name", default=None)
    p.add_argument("--preset", default=None)
    p.add_argument("--out", default=None)
    p.set_defaults(func=cmd_import)

    p = sub.add_parser("validate", help="validate a file")
    p.add_argument("path")
    p.add_argument("--type", choices=["tile", "mob", "item", "effect"], default=None)
    p.add_argument("--assets-java", default=str(BASE_DIR.parents[1] / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java"))
    p.set_defaults(func=cmd_validate)

    p = sub.add_parser("promote", help="promote staging asset into project assets")
    p.add_argument("src")
    p.add_argument("--target", "-t", default=None)
    p.add_argument("--type", choices=["tile", "mob", "item", "effect"], default=None)
    p.add_argument("--preset", default=None)
    p.add_argument("--force", action="store_true")
    p.add_argument("--assets-root", default=str(BASE_DIR.parents[1] / "core/src/main/assets"))
    p.add_argument("--assets-java", default=str(BASE_DIR.parents[1] / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java"))
    p.set_defaults(func=cmd_promote)

    p = sub.add_parser("palette", help="extract unique colors from image")
    p.add_argument("src")
    p.add_argument("--format", choices=["text", "json"], default="text")
    p.set_defaults(func=cmd_palette)

    p = sub.add_parser("variants", help="create N numbered copies of a base image")
    p.add_argument("base")
    p.add_argument("--count", type=int, default=3)
    p.add_argument("--out-dir", default=None)
    p.set_defaults(func=cmd_variants)

    p = sub.add_parser("info", help="show presets and conventions")
    p.set_defaults(func=cmd_info)

    p = sub.add_parser("list", help="list staging dir contents")
    p.set_defaults(func=cmd_list)

    p = sub.add_parser("compile", help="compile a .pxs script into .px.json")
    p.add_argument("script", help="path to .pxs script")
    p.add_argument("--out", "-o", default=None, help="output .px.json (default: <script stem>.px.json)")
    p.add_argument("--png", default=None, help="also render a .png to this path")
    p.add_argument("--dry-run", action="store_true", help="validate only, write nothing")
    p.add_argument("--preview", action="store_true", help="print an ASCII preview")
    p.set_defaults(func=cmd_compile)

    p = sub.add_parser("draw", help="run drawing commands on a target .px.json")
    p.add_argument("target", help="existing or new .px.json path")
    p.add_argument("commands", nargs="*", help='commands like "color 3a5f2a" or "rect 0 0 5 5"')
    p.add_argument("--png", default=None, help="also render a .png to this path")
    p.add_argument("--dry-run", action="store_true", help="validate only, write nothing")
    p.add_argument("--preview", action="store_true", help="print an ASCII preview")
    p.set_defaults(func=cmd_draw)

    p = sub.add_parser("preview", help="print an ASCII preview of an image or script")
    p.add_argument("src")
    p.add_argument("--width", type=int, default=0, help="wrap output at N columns")
    p.set_defaults(func=cmd_preview)

    p = sub.add_parser("selftest", help="run golden tests against tests/cases/")
    p.set_defaults(func=cmd_selftest)

    return parser


def main(argv=None):
    parser = build_parser()
    args = parser.parse_args(argv)
    if args.cmd == "variants" and args.out_dir is None:
        args.out_dir = str(_staging_dir(args) / "variants")
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
