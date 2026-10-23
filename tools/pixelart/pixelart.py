"""Core pixel-art helpers for Veiled Pixel Dungeon assets.

Shared by the agent (via ``import pixelart``) and by ``px.py``. The canonical
on-disk format is a ``.px.json`` file (see ``schema.json``); PNG is the
rendered output. Pixels are stored row-major as ``[r, g, b, a]`` with values
0-255.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Iterable, Optional, Tuple

from PIL import Image

Color = Tuple[int, int, int, int]


def _norm_color(color) -> Color:
    if isinstance(color, int):
        # grayscale shorthand
        return (color, color, color, 255)
    c = list(color)
    if len(c) == 3:
        c.append(255)
    if len(c) != 4:
        raise ValueError(f"color must have 3 or 4 components, got {color!r}")
    return tuple(int(round(v)) for v in c)


def new_image(
    name: str = "untitled",
    width: int = 16,
    height: int = 16,
    mode: str = "rgba",
    frame_width: Optional[int] = None,
    frame_height: Optional[int] = None,
    fill: Optional[Color] = None,
) -> dict:
    if width <= 0 or height <= 0:
        raise ValueError("width and height must be positive")
    if frame_width is None:
        frame_width = width
    if frame_height is None:
        frame_height = height
    base = (0, 0, 0, 0) if fill is None else _norm_color(fill)
    return {
        "name": name,
        "width": int(width),
        "height": int(height),
        "frameWidth": int(frame_width),
        "frameHeight": int(frame_height),
        "mode": mode,
        "pixels": [list(base) for _ in range(width * height)],
    }


def _idx(img: dict, x: int, y: int) -> int:
    w, h = img["width"], img["height"]
    if x < 0 or y < 0 or x >= w or y >= h:
        raise IndexError(f"pixel ({x},{y}) out of bounds for {w}x{h}")
    return y * w + x


def get_pixel(img: dict, x: int, y: int) -> Color:
    return tuple(img["pixels"][_idx(img, x, y)])


def set_pixel(img: dict, x: int, y: int, color) -> None:
    img["pixels"][_idx(img, x, y)] = list(_norm_color(color))


def _in_bounds(img, x, y):
    return 0 <= x < img["width"] and 0 <= y < img["height"]


def rect(img: dict, x: int, y: int, w: int, h: int, color, fill: bool = False) -> None:
    color = _norm_color(color)
    if fill:
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                if _in_bounds(img, xx, yy):
                    set_pixel(img, xx, yy, color)
    else:
        for xx in range(x, x + w):
            if _in_bounds(img, xx, y):
                set_pixel(img, xx, y, color)
            if _in_bounds(img, xx, y + h - 1):
                set_pixel(img, xx, y + h - 1, color)
        for yy in range(y, y + h):
            if _in_bounds(img, x, yy):
                set_pixel(img, x, yy, color)
            if _in_bounds(img, x + w - 1, yy):
                set_pixel(img, x + w - 1, yy, color)


def line(img: dict, x0: int, y0: int, x1: int, y1: int, color) -> None:
    color = _norm_color(color)
    # Bresenham
    dx = abs(x1 - x0)
    dy = -abs(y1 - y0)
    sx = 1 if x0 < x1 else -1
    sy = 1 if y0 < y1 else -1
    err = dx + dy
    x, y = x0, y0
    while True:
        if _in_bounds(img, x, y):
            set_pixel(img, x, y, color)
        if x == x1 and y == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x += sx
        if e2 <= dx:
            err += dx
            y += sy


def flood_fill(img: dict, x: int, y: int, color) -> None:
    color = _norm_color(color)
    if not _in_bounds(img, x, y):
        return
    target = get_pixel(img, x, y)
    if target == color:
        return
    stack = [(x, y)]
    while stack:
        cx, cy = stack.pop()
        if not _in_bounds(img, cx, cy):
            continue
        if get_pixel(img, cx, cy) != target:
            continue
        set_pixel(img, cx, cy, color)
        stack.extend([(cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)])


def eyedropper(img: dict, x: int, y: int) -> Optional[Color]:
    if not _in_bounds(img, x, y):
        return None
    return get_pixel(img, x, y)


def load_json(path) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    # normalize
    data.setdefault("frameWidth", data["width"])
    data.setdefault("frameHeight", data["height"])
    data.setdefault("mode", "rgba")
    px = data["pixels"]
    # flatten if rows of pixels were stored
    if (isinstance(px, list) and len(px) > 0 and isinstance(px[0], list)
            and len(px[0]) > 0 and isinstance(px[0][0], (list, tuple))):
        px = [c for row in px for c in row]
    data["pixels"] = [list(p) for p in px]
    return data


def save_json(img: dict, path) -> None:
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(img, f, indent=2, ensure_ascii=False)
        f.write("\n")


def render(img: dict, out_path) -> None:
    w, h = img["width"], img["height"]
    im = Image.new("RGBA", (w, h))
    flat = []
    for p in img["pixels"]:
        c = _norm_color(p)
        flat.append(c)
    im.putdata(flat)
    out_path = Path(out_path)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    im.save(out_path)


def import_png(path, name: Optional[str] = None) -> dict:
    im = Image.open(path).convert("RGBA")
    w, h = im.size
    if name is None:
        name = Path(path).stem
    pixels = [list(px) for px in im.getdata()]
    return {
        "name": name,
        "width": w,
        "height": h,
        "frameWidth": w,
        "frameHeight": h,
        "mode": "rgba",
        "pixels": pixels,
    }


def unique_colors(img_or_path) -> list:
    if isinstance(img_or_path, (str, Path)):
        img = import_png(img_or_path)
    else:
        img = img_or_path
    seen = set()
    out = []
    for p in img["pixels"]:
        c = tuple(_norm_color(p))
        if c not in seen:
            seen.add(c)
            out.append(list(c))
    return out


def flip_h(img: dict) -> dict:
    import copy
    out = copy.deepcopy(img)
    w = img["width"]
    for y in range(img["height"]):
        for x in range(w):
            out["pixels"][y * w + x] = list(img["pixels"][y * w + (w - 1 - x)])
    return out


def flip_v(img: dict) -> dict:
    import copy
    out = copy.deepcopy(img)
    w, h = img["width"], img["height"]
    for y in range(h):
        for x in range(w):
            out["pixels"][y * w + x] = list(img["pixels"][(h - 1 - y) * w + x])
    return out


def rotate_90(img: dict) -> dict:
    w, h = img["width"], img["height"]
    out = new_image(img["name"], h, w, img["mode"], img["frameHeight"], img["frameWidth"])
    for oy in range(h):
        for ox in range(w):
            out["pixels"][ox * h + (h - 1 - oy)] = list(img["pixels"][oy * w + ox])
    return out


def scale_nearest(img: dict, factor: int) -> dict:
    if factor < 1:
        raise ValueError("factor must be >= 1")
    w, h = img["width"], img["height"]
    out = new_image(img["name"], w * factor, h * factor, img["mode"],
                    img["frameWidth"] * factor, img["frameHeight"] * factor)
    for y in range(h):
        for x in range(w):
            c = img["pixels"][y * w + x]
            for fy in range(factor):
                for fx in range(factor):
                    out["pixels"][(y * factor + fy) * (w * factor) + (x * factor + fx)] = list(c)
    return out