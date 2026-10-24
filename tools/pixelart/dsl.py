"""Pixel-art script DSL (pxs v1) for Veiled Pixel Dungeon assets (agent side).

Canonical interpreter for `.pxs` scripts. `pxscript.js` is a 1:1 port for the
web editor; both are kept in sync by the golden cases in `tests/cases/`.

Grammar (line-based, `#` starts a comment up to end of line):

    canvas W H          <- mandatory (sets size, clears, resets mirror)
    name <snake_case>   <- optional
    frame W H           <- optional frame strip boundaries
    bg <color>          <- fills the whole canvas

    color <color>       <- sets current color: "3a5f2a" | "3a5f2a80" |
                           "rgb r g b" | "rgb r g b a"
    point x y [color]
    line x1 y1 x2 y2 [color]
    rect x y w h [color]
    rectfill x y w h [color]
    circle cx cy r [color]
    circlefill cx cy r [color]
    fill x y [color]    <- flood fill on exact RGBA match
    outline [color] [thickness]
    shade amount        <- adds amount to RGB of non-transparent pixels
    mirror x|y|xy|off   <- symmetry mode for subsequent draw commands
    shift dx dy         <- moves the whole image, transparent fill
    repeat N {          <- multiline block: `repeat N {` then body, then `}`

Out-of-bounds pixels are clipped; a shape that lies completely outside the
canvas adds a warning (not an error). No anti-aliasing.

Data model: pixelart img dict, ``pixels`` is a row-major list of ``[r,g,b,a]``
lists (one per pixel).
"""

from __future__ import annotations

import re
from pixelart import new_image

HEX_CHARS = set("0123456789abcdefABCDEF")
INT_RE = re.compile(r"^[-+]?[0-9]+$")
SNAKE_RE = re.compile(r"^[a-z][a-z0-9_]*$")


class PxsError(Exception):
    """Parse/semantic error with line number."""

    def __init__(self, msg, line=None):
        self.msg = msg
        self.line = line
        super().__init__(self.__str__())

    def __str__(self):
        if self.line is None:
            return self.msg
        return f"line {self.line}: {self.msg}"


# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

def _strip_comment(raw_line):
    return raw_line.split("#", 1)[0].strip()


def parse(text):
    """Parse a pxs script into a nested command list.

    Nodes: {"cmd": str, "args": [str], "line": int} and
           {"cmd": "repeat", "count": int, "body": [...], "line": int}
    """
    root = []
    stack = [root]
    lines = text.splitlines()
    for lineno, raw in enumerate(lines, 1):
        line = _strip_comment(raw)
        if not line:
            continue
        toks = line.split()
        if toks[0] == "}":
            if len(stack) == 1:
                raise PxsError("unexpected '}'", lineno)
            stack.pop()
            continue
        if toks[0] == "repeat":
            if len(toks) != 3 or toks[2] != "{":
                raise PxsError("use multiline form: `repeat N {` ... `}`", lineno)
            if not INT_RE.match(toks[1]):
                raise PxsError(f"repeat count must be an integer, got {toks[1]}", lineno)
            count = int(toks[1])
            if count < 0:
                raise PxsError(f"repeat count must be >= 0, got {count}", lineno)
            node = {"cmd": "repeat", "count": count, "body": [], "line": lineno}
            stack[-1].append(node)
            stack.append(node["body"])
            continue
        if "{" in line or "}" in line:
            raise PxsError("'{' must end a `repeat N {` line; '}' stands alone", lineno)
        stack[-1].append({"cmd": toks[0], "args": toks[1:], "line": lineno})
    if len(stack) != 1:
        raise PxsError(f"missing {len(stack) - 1} closing brace(s) '}}'", len(lines))
    return root


# ---------------------------------------------------------------------------
# Colors
# ---------------------------------------------------------------------------

def _parse_color(toks, line):
    """Parse a color from the front of `toks`; return (rgba, consumed_count)."""
    if not toks:
        raise PxsError("missing color", line)
    if toks[0].lower() == "rgb":
        if len(toks) not in (4, 5):
            raise PxsError("rgb needs 3 or 4 numbers", line)
        vals = []
        for t in toks[1:4]:
            if not INT_RE.match(t):
                raise PxsError(f"rgb expects numbers, got {t}", line)
            vals.append(max(0, min(255, int(t))))
        if len(toks) == 5:
            if not INT_RE.match(toks[4]):
                raise PxsError(f"rgb alpha must be a number, got {toks[4]}", line)
            vals.append(max(0, min(255, int(toks[4]))))
        else:
            vals.append(255)
        return vals, len(toks) - 1
    hx = toks[0]
    if len(hx) in (6, 8) and all(c in HEX_CHARS for c in hx):
        r, g, b = int(hx[0:2], 16), int(hx[2:4], 16), int(hx[4:6], 16)
        a = int(hx[6:8], 16) if len(hx) == 8 else 255
        return [r, g, b, a], 1
    raise PxsError(f"hex color must be 6 or 8 hex digits, got {toks[0]}", line)


def _trailing_color(toks, line):
    """Return (rgba or None, remaining_args); trailing colors: hex or rgb form.

    The color sits at the END of the argument list, so we probe from the back.
    """
    if not toks:
        return None, toks
    # rgb form: "rgb r g b" | "rgb r g b a" as a suffix
    for n in (5, 4):
        if len(toks) > n - 1 and toks[-n].lower() == "rgb":
            color, _ = _parse_color(toks[-n:], line)
            return color, toks[:-n]
    hx = toks[-1]
    if len(hx) in (6, 8) and all(c in HEX_CHARS for c in hx):
        color, _ = _parse_color(toks[-1:], line)
        return color, toks[:-1]
    return None, toks


def _leading_color(toks, line):
    """Return (rgba or None, remaining_args); color at the FRONT of `toks`.

    Used by `outline [color] [thickness]`. For the rgb form the numbers are
    always the color (3 or 4 numbers = rgb+alpha); a thickness then follows.
    """
    if not toks:
        return None, toks
    if toks[0].lower() == "rgb":
        if len(toks) not in (4, 5):
            raise PxsError("rgb needs 3 or 4 numbers", line)
        color, _ = _parse_color(toks, line)
        return color, []
    hx = toks[0]
    if len(hx) in (6, 8) and all(c in HEX_CHARS for c in hx):
        color, _ = _parse_color(toks[:1], line)
        return color, toks[1:]
    return None, toks


# ---------------------------------------------------------------------------
# Interpreter
# ---------------------------------------------------------------------------

class Script:
    """Executes a parsed pxs program, one image at a time."""

    def __init__(self, tree, warnings=None):
        self.tree = tree
        self.warnings = warnings if warnings is not None else []
        self.img = None
        self.mirror = None  # None | "x" | "y" | "xy"
        self.color = [0, 0, 0, 255]
        self._pending_name = None
        self._canvas_seen = False

    # entry point ------------------------------------------------------------
    def run(self, img=None, *, require_canvas=False):
        self.img = img
        for node in self.tree:
            self._exec(node)
        if self.img is not None and self._pending_name:
            self.img["name"] = self._pending_name
        if require_canvas and not self._canvas_seen:
            raise PxsError("script needs a `canvas W H` command")
        return self.img

    # dispatch ---------------------------------------------------------------
    def _exec(self, node):
        cmd = node["cmd"]
        line = node.get("line")
        if cmd == "repeat":
            for _ in range(node["count"]):
                for n in node["body"]:
                    self._exec(n)
            return
        handler = getattr(self, "_cmd_" + cmd, None)
        if handler is None or not callable(handler):
            raise PxsError(f"unknown command {cmd}", line)
        handler(node, node.get("args", []), line)

    def _warn(self, msg):
        if msg not in self.warnings:
            self.warnings.append(msg)

    def _int(self, t, line, what):
        if not INT_RE.match(t):
            raise PxsError(f"{what} must be an integer, got {t}", line)
        return int(t)

    def _need(self, args, n, line, what):
        if len(args) != n:
            raise PxsError(f"{what} needs exactly {n} argument(s)", line)

    def _ensure(self, what, line):
        if self.img is None:
            raise PxsError(f"{what} needs a canvas first", line)

    # pixel writes -------------------------------------------------------------
    def _set(self, x, y, color):
        """Direct pixel write without mirroring; silent-out-of-bounds."""
        img = self.img
        w, h = img["width"], img["height"]
        if 0 <= x < w and 0 <= y < h:
            img["pixels"][y * w + x] = [color[0], color[1], color[2], color[3]]

    def _put(self, x, y, color):
        """Pixel write honoring mirror symmetry."""
        self._set(x, y, color)
        if self.mirror is None:
            return
        w = self.img["width"]
        h = self.img["height"]
        if self.mirror in ("x", "xy"):
            self._set(w - 1 - x, y, color)
        if self.mirror in ("y", "xy"):
            self._set(x, h - 1 - y, color)
        if self.mirror == "xy":
            self._set(w - 1 - x, h - 1 - y, color)

    def _bbox_outside(self, x0, y0, x1, y1):
        w, h = self.img["width"], self.img["height"]
        return max(x0, x1) < 0 or min(x0, x1) >= w or max(y0, y1) < 0 or min(y0, y1) >= h

    def _target_color(self, color, line=None):
        return color if color is not None else self.color

    # canvas-level commands -----------------------------------------------------
    def _cmd_canvas(self, node, args, line):
        self._need(args, 2, line, "canvas")
        w = self._int(args[0], line, "canvas width")
        h = self._int(args[1], line, "canvas height")
        if w < 1 or h < 1:
            raise PxsError(f"canvas size must be >= 1, got {w}x{h}", line)
        name = self.img["name"] if self.img is not None else "untitled"
        self.img = new_image(name, w, h)
        self.mirror = None
        self._canvas_seen = True

    def _cmd_name(self, node, args, line):
        self._need(args, 1, line, "name")
        if not SNAKE_RE.match(args[0]):
            raise PxsError(f"name {args[0]} is not snake_case", line)
        if self.img is None:
            self._pending_name = args[0]
        else:
            self.img["name"] = args[0]

    def _cmd_frame(self, node, args, line):
        self._need(args, 2, line, "frame")
        fw = self._int(args[0], line, "frame width")
        fh = self._int(args[1], line, "frame height")
        self._ensure("frame", line)
        if fw < 1 or fh < 1:
            raise PxsError(f"frame size must be >= 1, got {fw}x{fh}", line)
        w, h = self.img["width"], self.img["height"]
        if w % fw != 0:
            self._warn(f"frame width {fw} does not evenly divide canvas width {w} (line {line})")
        if h % fh != 0:
            self._warn(f"frame height {fh} does not evenly divide canvas height {h} (line {line})")
        self.img["frameWidth"] = fw
        self.img["frameHeight"] = fh

    def _cmd_bg(self, node, args, line):
        self._need(args, 1, line, "bg")
        color, _ = _parse_color(args, line)
        self._ensure("bg", line)
        w, h = self.img["width"], self.img["height"]
        for y in range(h):
            for x in range(w):
                self._set(x, y, color)

    # state ------------------------------------------------------------------------
    def _cmd_color(self, node, args, line):
        if not args:
            raise PxsError("color needs a value", line)
        self.color, _ = _parse_color(args, line)

    def _cmd_mirror(self, node, args, line):
        self._need(args, 1, line, "mirror")
        m = args[0].lower()
        if m not in ("x", "y", "xy", "off"):
            raise PxsError(f"mirror mode must be x|y|xy|off, got {args[0]}", line)
        self.mirror = None if m == "off" else m

    def _cmd_shift(self, node, args, line):
        self._need(args, 2, line, "shift")
        dx = self._int(args[0], line, "shift dx")
        dy = self._int(args[1], line, "shift dy")
        self._ensure("shift", line)
        img = self.img
        w, h = img["width"], img["height"]
        src = [list(p) for p in img["pixels"]]
        out = [[0, 0, 0, 0] for _ in range(w * h)]
        for y in range(h):
            for x in range(w):
                tx, ty = x + dx, y + dy
                if 0 <= tx < w and 0 <= ty < h:
                    out[ty * w + tx] = src[y * w + x]
        img["pixels"] = out

    # drawing ------------------------------------------------------------------------
    def _cmd_point(self, node, args, line):
        self._ensure("point", line)
        color, rem = _trailing_color(args, line)
        if len(rem) != 2:
            raise PxsError("point needs exactly 2 coordinates", line)
        x = self._int(rem[0], line, "point x")
        y = self._int(rem[1], line, "point y")
        c = self._target_color(color)
        w, h = self.img["width"], self.img["height"]
        if not (0 <= x < w and 0 <= y < h):
            self._warn(f"point at line {line} is entirely outside the canvas")
            return
        self._put(x, y, c)

    def _cmd_line(self, node, args, line):
        self._ensure("line", line)
        color, rem = _trailing_color(args, line)
        if len(rem) != 4:
            raise PxsError("line needs 4 coordinates", line)
        x0, y0, x1, y1 = (self._int(v, line, "line coord") for v in rem)
        c = self._target_color(color)
        if self._bbox_outside(min(x0, x1), min(y0, y1), max(x0, x1), max(y0, y1)):
            self._warn(f"line at line {line} is entirely outside the canvas")
        if x0 == x1 and y0 == y1:
            self._put(x0, y0, c)
            return
        dx = abs(x1 - x0)
        dy = -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        cx, cy = x0, y0
        while True:
            self._put(cx, cy, c)
            if cx == x1 and cy == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                cx += sx
            if e2 <= dx:
                err += dx
                cy += sy

    def _cmd_rect(self, node, args, line):
        self._rect(node, args, line, fill=False)

    def _cmd_rectfill(self, node, args, line):
        self._rect(node, args, line, fill=True)

    def _rect(self, node, args, line, fill=False):
        self._ensure("rect", line)
        color, rem = _trailing_color(args, line)
        if len(rem) != 4:
            raise PxsError("rect needs x y w h", line)
        x, y, w, h = (self._int(v, line, "rect coord") for v in rem)
        if w < 0 or h < 0:
            raise PxsError(f"rect w/h must be >= 0, got {w}x{h}", line)
        c = self._target_color(color)
        if self._bbox_outside(x, y, x + w - 1, y + h - 1):
            self._warn(f"rect at line {line} is entirely outside the canvas")
        if fill:
            for yy in range(y, y + h):
                for xx in range(x, x + w):
                    self._put(xx, yy, c)
            return
        for xx in range(x, x + w):
            self._put(xx, y, c)
            if h > 1:
                self._put(xx, y + h - 1, c)
        for yy in range(y, y + h):
            self._put(x, yy, c)
            if w > 1:
                self._put(x + w - 1, yy, c)

    def _cmd_circle(self, node, args, line):
        self._circle(node, args, line, fill=False)

    def _cmd_circlefill(self, node, args, line):
        self._circle(node, args, line, fill=True)

    def _circle(self, node, args, line, fill=False):
        self._ensure("circle", line)
        color, rem = _trailing_color(args, line)
        if len(rem) != 3:
            raise PxsError("circle needs cx cy r", line)
        cx, cy, r = (self._int(v, line, "circle coord") for v in rem)
        if r < 0:
            raise PxsError(f"circle r must be >= 0, got {r}", line)
        c = self._target_color(color)
        if self._bbox_outside(cx - r, cy - r, cx + r, cy + r):
            self._warn(f"circle at line {line} is entirely outside the canvas")
            return
        r2 = r * r
        inner = (r - 1) * (r - 1) if not fill else -1
        for dy in range(-r, r + 1):
            for dx in range(-r, r + 1):
                d2 = dx * dx + dy * dy
                if d2 <= r2 and d2 > inner:
                    self._put(cx + dx, cy + dy, c)

    def _cmd_fill(self, node, args, line):
        self._ensure("fill", line)
        color, rem = _trailing_color(args, line)
        if len(rem) != 2:
            raise PxsError("fill needs x y", line)
        x = self._int(rem[0], line, "fill x")
        y = self._int(rem[1], line, "fill y")
        c = self._target_color(color)
        w, h = self.img["width"], self.img["height"]
        if not (0 <= x < w and 0 <= y < h):
            self._warn(f"fill at line {line} is entirely outside the canvas")
            return
        seeds = [(x, y)]
        if self.mirror in ("x", "xy"):
            seeds.append((w - 1 - x, y))
        if self.mirror in ("y", "xy"):
            seeds.append((x, h - 1 - y))
        if self.mirror == "xy":
            seeds.append((w - 1 - x, h - 1 - y))
        for sx, sy in seeds:
            self._flood(sx, sy, c)

    def _flood(self, x, y, color):
        img = self.img
        w, h = img["width"], img["height"]

        def get(px, py):
            return tuple(img["pixels"][py * w + px])

        target = get(x, y)
        if target == (color[0], color[1], color[2], color[3]):
            return

        def is_target(px, py):
            if not (0 <= px < w and 0 <= py < h):
                return False
            pxv = get(px, py)
            return pxv == tuple(target)

        stack = [(x, y)]
        while stack:
            cx, cy = stack.pop()
            if not is_target(cx, cy):
                continue
            self._set(cx, cy, color)
            stack.extend([(cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)])

    def _cmd_outline(self, node, args, line):
        self._ensure("outline", line)
        color, rem = _leading_color(args, line)
        if rem:
            if len(rem) != 1:
                raise PxsError("outline takes [color] [thickness]", line)
            d = self._int(rem[0], line, "outline thickness")
            if d < 1:
                raise PxsError(f"outline thickness must be >= 1, got {d}", line)
        else:
            d = 1
        c = self._target_color(color)
        img = self.img
        w, h = img["width"], img["height"]
        opaque = set()
        for y0 in range(h):
            for x0 in range(w):
                if img["pixels"][y0 * w + x0][3] != 0:
                    opaque.add((x0, y0))
        if not opaque:
            self._warn(f"outline at line {line} found nothing to outline")
            return
        for _ring in range(d):
            boundary = set()
            for x0 in range(w):
                for y0 in range(h):
                    if (x0, y0) in opaque:
                        continue
                    if ((x0 - 1, y0) in opaque or (x0 + 1, y0) in opaque or
                            (x0, y0 - 1) in opaque or (x0, y0 + 1) in opaque):
                        boundary.add((x0, y0))
            if not boundary:
                break
            for (x0, y0) in boundary:
                self._set(x0, y0, c)
                opaque.add((x0, y0))

    def _cmd_shade(self, node, args, line):
        self._ensure("shade", line)
        self._need(args, 1, line, "shade")
        a = self._int(args[0], line, "shade amount")
        if a < -255 or a > 255:
            self._warn(f"shade amount {a} clamped to [-255, 255]")
            a = max(-255, min(255, a))
        img = self.img
        w, h = img["width"], img["height"]
        for y0 in range(h):
            for x0 in range(w):
                px = img["pixels"][y0 * w + x0]
                if px[3] != 0:
                    px[0] = max(0, min(255, px[0] + a))
                    px[1] = max(0, min(255, px[1] + a))
                    px[2] = max(0, min(255, px[2] + a))
                    img["pixels"][y0 * w + x0] = px


def run(text, img=None, *, require_canvas=False):
    """Parse and run a pxs script; return (img, warnings)."""
    tree = parse(text)
    script = Script(tree)
    img = script.run(img, require_canvas=require_canvas)
    return img, script.warnings
