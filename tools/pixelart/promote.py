"""Promote validated staging assets into the project's assets directory."""

import argparse
import os
import shutil
import sys
from pathlib import Path

from pixelart import load_json, render, import_png
from validate import validate_path
from presets import PRESETS

REPO_ROOT = Path(__file__).resolve().parents[1]
ASSETS_ROOT = REPO_ROOT / "core" / "src" / "main" / "assets"
ASSETS_JAVA = REPO_ROOT / "core" / "src" / "main" / "java" / "com" / "shatteredpixel" / "shatteredpixeldungeon" / "Assets.java"

SUBDIR_MAP = {
    "tile": "environment/custom_tiles",
    "mob": "sprites",
    "item": "sprites",
    "effect": "effects",
}


def _asset_type_from_preset(preset_name):
    if preset_name is None:
        return None
    name = preset_name.lower()
    if name.startswith("tile"):
        return "tile"
    if name.startswith("mob"):
        return "mob"
    if name.startswith("item"):
        return "item"
    if name.startswith("effect") or name.startswith("fx"):
        return "effect"
    return None


def promote(src, target_subdir=None, asset_type=None, preset=None, force=False,
           assets_root=ASSETS_ROOT, assets_java=ASSETS_JAVA):
    src = Path(src)
    if not src.exists():
        return (False, [f"source not found: {src}"])

    # determine target subdir
    if target_subdir is None:
        if preset and preset in PRESETS:
            target_subdir = PRESETS[preset]["target_dir"]
        elif asset_type in SUBDIR_MAP:
            target_subdir = SUBDIR_MAP[asset_type]
        else:
            target_subdir = ""
    if asset_type is None and preset:
        asset_type = _asset_type_from_preset(preset)

    issues = validate_path(src, asset_type=asset_type, assets_java_path=None)
    hard = [i for i in issues if not i.startswith("WARNING:") and not i.startswith("NOTE:")]
    if hard:
        return (False, issues)

    # render png if source is json
    if src.suffix.lower() in (".json",) or str(src).endswith(".px.json"):
        data = load_json(src)
        name = data.get("name", src.stem.split('.')[0])
        png_name = name + ".png"
        tmp_png = src.parent / (name + ".png")
        render(data, tmp_png)
        src_png = tmp_png
    else:
        src_png = src
        name = src.stem
        png_name = src.name

    if not png_name.endswith(".png"):
        png_name += ".png"

    # snake_case check on name
    import re
    if not re.match(r"^[a-z][a-z0-9_]*$", Path(png_name).stem):
        return (False, [f"target name {png_name!r} not snake_case"])

    dest_dir = Path(assets_root) / target_subdir if target_subdir else Path(assets_root)
    dest_dir.mkdir(parents=True, exist_ok=True)
    dest = dest_dir / png_name

    if dest.exists() and not force:
        return (False, [f"destination exists: {dest} (use --force to overwrite)"])

    shutil.copy2(src_png, dest)
    # clean up rendered temp png
    if src_png != src and src_png.exists():
        try:
            src_png.unlink()
        except OSError:
            pass

    # Assets.java hint
    rel = dest.relative_to(assets_root).as_posix()
    hint_issues = []
    if assets_java and Path(assets_java).exists():
        text = Path(assets_java).read_text(encoding="utf-8")
        if rel not in text:
            hint_issues.append(
                f"NOTE: add a constant in Assets.java for {rel!r}, e.g.\n"
                f"  public static final String {name.upper()} = \"{rel}\";"
            )

    msgs = [f"promoted: {src} -> {dest}"]
    msgs.extend(hint_issues)
    return (True, msgs)


def main(argv=None):
    parser = argparse.ArgumentParser(description="Promote a staging asset into the project assets.")
    parser.add_argument("src", help="source .px.json or .png in staging")
    parser.add_argument("--target", "-t", default=None, help="target subdir relative to assets root (e.g. environment/custom_tiles)")
    parser.add_argument("--type", choices=["tile", "mob", "item", "effect"], default=None, help="asset type for validation")
    parser.add_argument("--preset", default=None, help="preset name (determines target dir + type)")
    parser.add_argument("--force", action="store_true", help="overwrite existing destination")
    parser.add_argument("--assets-root", default=str(ASSETS_ROOT), help="override assets root")
    parser.add_argument("--assets-java", default=str(ASSETS_JAVA), help="override Assets.java path")
    args = parser.parse_args(argv)

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


if __name__ == "__main__":
    sys.exit(main())
