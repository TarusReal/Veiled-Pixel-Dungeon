"""Validation rules for Veiled Pixel Dungeon pixel-art assets."""

import os
import re
import json
from pathlib import Path

from pixelart import load_json, import_png, new_image

SNAKE_RE = re.compile(r"^[a-z][a-z0-9_]*$")


def _issues_list():
    return []


def validate_json(data, asset_type=None, filename=None):
    issues = []
    # required fields
    for k in ("name", "width", "height", "pixels"):
        if k not in data:
            issues.append(f"missing field '{k}'")
            return issues
    w, h = data["width"], data["height"]
    if not isinstance(w, int) or not isinstance(h, int) or w <= 0 or h <= 0:
        issues.append(f"invalid dimensions {w}x{h}")
        return issues
    px = data["pixels"]
    if (isinstance(px, list) and len(px) > 0 and isinstance(px[0], list)
            and len(px[0]) > 0 and isinstance(px[0][0], (list, tuple))):
        px = [c for row in px for c in row]
    data["pixels"] = px
    expected = w * h
    if len(data["pixels"]) != expected:
        issues.append(f"pixels length {len(data['pixels'])} != {expected} (width*height)")
    # pixel values
    bad = 0
    for i, p in enumerate(data["pixels"]):
        if not isinstance(p, (list, tuple)) or len(p) not in (3, 4):
            bad += 1
            if bad < 5:
                issues.append(f"pixel {i} malformed: {p!r}")
            continue
        for v in p:
            if not isinstance(v, int) or v < 0 or v > 255:
                bad += 1
                if bad < 5:
                    issues.append(f"pixel {i} value out of range: {v}")
                break
    # dimension rules by type
    _check_dims(issues, w, h, asset_type)
    # filename
    if filename is not None:
        _check_filename(issues, filename)
    return issues


def validate_png(path, asset_type=None):
    issues = []
    from PIL import Image
    try:
        im = Image.open(path)
    except Exception as e:
        return [f"cannot open png: {e}"]
    w, h = im.size
    _check_dims(issues, w, h, asset_type)
    _check_filename(issues, Path(path).stem)
    # mode check: warn if not RGBA/P
    if im.mode not in ("RGBA", "P", "RGB", "L", "1"):
        issues.append(f"unusual image mode {im.mode}")
    return issues


def _check_dims(issues, w, h, asset_type):
    if asset_type is None:
        return
    rule = None
    from presets import ASSET_TYPES
    rule = ASSET_TYPES.get(asset_type)
    if not rule:
        return
    if "multiple_of" in rule:
        mx, my = rule["multiple_of"]
        if w % mx != 0:
            issues.append(f"width {w} not multiple of {mx}")
        if h % my != 0:
            issues.append(f"height {h} not multiple of {my}")
    if "size" in rule:
        sx, sy = rule["size"]
        if (w, h) != (sx, sy):
            issues.append(f"size {w}x{h} != expected {sx}x{sy}")
    if "frame" in rule:
        fx, fy = rule["frame"]
        if w % fx != 0:
            issues.append(f"width {w} not multiple of frame width {fx}")
        if h != fy and h % fy != 0:
            issues.append(f"height {h} not compatible with frame height {fy}")


def _stem(name):
    s = str(name)
    for suf in (".px.json", ".json", ".png"):
        if s.endswith(suf):
            s = s[: -len(suf)]
            break
    return s


def _check_filename(issues, name):
    stem = _stem(name)
    if not SNAKE_RE.match(stem):
        issues.append(f"filename {stem!r} is not snake_case (expected ^[a-z][a-z0-9_]*$)")


def check_assets_constant(assets_java_path, filename):
    """Return list of issues (warnings) if no constant references the file."""
    issues = []
    if not os.path.exists(assets_java_path):
        return issues
    with open(assets_java_path, encoding="utf-8") as f:
        text = f.read()
    # normalize filename to forward slashes relative to assets root
    rel = filename.replace(os.sep, "/")
    if rel not in text:
        issues.append(f"WARNING: no reference to {rel!r} found in Assets.java (add a constant)")
    return issues


def validate_path(path, asset_type=None, assets_java_path=None):
    """Validate a file (.px.json or .png) and return list of issues."""
    path = Path(path)
    issues = []
    if path.suffix == ".json" or str(path).endswith(".px.json"):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except Exception as e:
            return [f"cannot parse json: {e}"]
        issues = validate_json(data, asset_type=asset_type, filename=path.name)
    elif path.suffix.lower() == ".png":
        issues = validate_png(path, asset_type=asset_type)
    else:
        issues.append(f"unsupported file type {path.suffix}")
    if assets_java_path:
        check_name = path.name
        if check_name.endswith(".px.json") or check_name.endswith(".json"):
            check_name = check_name.replace(".px.json", ".png").replace(".json", ".png")
        issues.extend(check_assets_constant(assets_java_path, check_name))
    return issues


def is_hard_issue(issue):
    return not (issue.startswith("WARNING:") or issue.startswith("NOTE:"))


def is_valid(path, asset_type=None, assets_java_path=None):
    issues = validate_path(path, asset_type=asset_type, assets_java_path=assets_java_path)
    return all(not is_hard_issue(i) for i in issues)
