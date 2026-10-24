/* pxscript.js — 1:1 port of tools/pixelart/dsl.py (pxs v1).
 * Used by editor.html to compile the recipe textarea; validated against the
 * golden cases in tests/cases/ by tests/run_js.js. Keep in sync with dsl.py.
 */
const Pxs = (function () {
  const HEX = "0123456789abcdefABCDEF";
  const SNAKE_RE = /^[a-z][a-z0-9_]*$/;
  const INT_RE = /^[-+]?[0-9]+$/;

  function PxsError(msg, line) {
    this.msg = msg;
    this.line = line;
    this.isPxsError = true;
    this.toString = function () {
      return line != null ? `line ${line}: ${msg}` : msg;
    };
  }
  PxsError.isError = function (e) { return e && e.isPxsError === true; };

  // parsing -------------------------------------------------------------------
  function stripComment(raw) {
    const i = raw.indexOf("#");
    return i < 0 ? raw.trim() : raw.slice(0, i).trim();
  }

  function parse(text) {
    const root = [];
    const stack = [root];
    const lines = text.split(/\r\n|\n\r|\n/);
    for (let lineno = 1; lineno <= lines.length; lineno++) {
      const line = stripComment(lines[lineno - 1]);
      if (!line) continue;
      const toks = line.split(/\s+/).filter(Boolean);
      if (toks[0] === "}") {
        if (stack.length === 1) throw new PxsError("unexpected '}'", lineno);
        stack.pop();
        continue;
      }
      if (toks[0] === "repeat") {
        if (toks.length !== 3 || toks[2] !== "{") {
          throw new PxsError("use multiline form: `repeat N {` ... `}`", lineno);
        }
        if (!INT_RE.test(toks[1])) {
          throw new PxsError(`repeat count must be an integer, got ${toks[1]}`, lineno);
        }
        const count = parseInt(toks[1], 10);
        if (count < 0) throw new PxsError(`repeat count must be >= 0, got ${count}`, lineno);
        const node = { cmd: "repeat", count: count, body: [], line: lineno };
        stack[stack.length - 1].push(node);
        stack.push(node.body);
        continue;
      }
      if (line.indexOf("{") >= 0 || line.indexOf("}") >= 0) {
        throw new PxsError("'{' must end a `repeat N {` line; '}' stands alone", lineno);
      }
      stack[stack.length - 1].push({ cmd: toks[0], args: toks.slice(1), line: lineno });
    }
    if (stack.length !== 1) {
      throw new PxsError(`missing ${stack.length - 1} closing brace(s) '}'`, lines.length);
    }
    return root;
  }

  // colors --------------------------------------------------------------------
  function clampByte(v) { return Math.max(0, Math.min(255, v)); }

  function parseColor(toks, line) {
    if (!toks || toks.length === 0) throw new PxsError("missing color", line);
    if (toks[0].toLowerCase() === "rgb") {
      if (toks.length !== 4 && toks.length !== 5) {
        throw new PxsError("rgb needs 3 or 4 numbers", line);
      }
      const vals = [];
      for (const t of toks.slice(1, 4)) {
        if (!/^[+-]?\d+(\.\d+)?$/.test(t)) throw new PxsError(`rgb expects numbers, got ${t}`, line);
        vals.push(clampByte(Math.round(Number(t))));
      }
      if (toks.length === 5) {
        if (!/^[+-]?\d+(\.\d+)?$/.test(toks[4])) {
          throw new PxsError(`rgb alpha must be a number, got ${toks[4]}`, line);
        }
        vals.push(clampByte(Math.round(Number(toks[4]))));
      } else {
        vals.push(255);
      }
      return { color: vals, consumed: toks.length - 1 };
    }
    const hx = toks[0];
    if ((hx.length === 6 || hx.length === 8) && hx.split("").every(c => HEX.indexOf(c) >= 0)) {
      const color = [
        parseInt(hx.slice(0, 2), 16),
        parseInt(hx.slice(2, 4), 16),
        parseInt(hx.slice(4, 6), 16),
        hx.length === 8 ? parseInt(hx.slice(6, 8), 16) : 255
      ];
      return { color: color, consumed: 1 };
    }
    throw new PxsError(`hex color must be 6 or 8 hex digits, got ${toks[0]}`, line);
  }

  // Trailing colors sit at the END of the arg list, so probe from the back.
  function trailingColor(toks, line) {
    if (!toks || toks.length === 0) return { color: null, rest: [] };
    // rgb form: "rgb r g b" | "rgb r g b a" as a suffix
    for (const n of [5, 4]) {
      if (toks.length > n - 1 && toks[toks.length - n].toLowerCase() === "rgb") {
        const res = parseColor(toks.slice(toks.length - n), line);
        return { color: res.color, rest: toks.slice(0, toks.length - n) };
      }
    }
    const hx = toks[toks.length - 1];
    if ((hx.length === 6 || hx.length === 8) && hx.split("").every(c => HEX.indexOf(c) >= 0)) {
      const res = parseColor([hx], line);
      return { color: res.color, rest: toks.slice(0, toks.length - 1) };
    }
    return { color: null, rest: toks.slice() };
  }

  // Color at the FRONT of the arg list (used by `outline [color] [thickness]`).
  function leadingColor(toks, line) {
    if (!toks || toks.length === 0) return { color: null, rest: [] };
    if (toks[0].toLowerCase() === "rgb") {
      if (toks.length !== 4 && toks.length !== 5) {
        throw new PxsError("rgb needs 3 or 4 numbers", line);
      }
      return { color: parseColor(toks, line).color, rest: [] };
    }
    const hx = toks[0];
    if ((hx.length === 6 || hx.length === 8) && hx.split("").every(c => HEX.indexOf(c) >= 0)) {
      const res = parseColor([hx], line);
      return { color: res.color, rest: toks.slice(1) };
    }
    return { color: null, rest: toks.slice() };
  }

  // image ---------------------------------------------------------------------
  function newImage(name, w, h) {
    return {
      name: name,
      width: w,
      height: h,
      frameWidth: w,
      frameHeight: h,
      mode: "rgba",
      pixels: Array.from({ length: w * h }, () => [0, 0, 0, 0])
    };
  }

  // interpreter ---------------------------------------------------------------
  function Script(tree, warnings) {
    this.tree = tree;
    this.warnings = warnings || [];
    this.img = null;
    this.mirror = null;
    this.color = [0, 0, 0, 255];
    this.pendingName = null;
    this.canvasSeen = false;
  }

  Script.prototype.run = function (img, requireCanvas) {
    this.img = img || null;
    this.execAll(this.tree);
    if (this.img && this.pendingName) this.img.name = this.pendingName;
    if (requireCanvas && !this.canvasSeen) throw new PxsError("script needs a `canvas W H` command", null);
    return this.img;
  };

  Script.prototype.execAll = function (nodes) { for (const n of nodes) this.exec(n); };

  Script.prototype.exec = function (node) {
    if (node.cmd === "repeat") {
      for (let i = 0; i < node.count; i++) this.execAll(node.body);
      return;
    }
    const h = this["cmd_" + node.cmd];
    if (typeof h !== "function") throw new PxsError(`unknown command ${node.cmd}`, node.line);
    h.call(this, node, node.args, node.line);
  };

  Script.prototype.warn = function (msg) {
    if (this.warnings.indexOf(msg) < 0) this.warnings.push(msg);
  };

  Script.prototype.int = function (t, line, what) {
    if (!INT_RE.test(t)) throw new PxsError(`${what} must be an integer, got ${t}`, line);
    return parseInt(t, 10);
  };

  Script.prototype.need = function (args, n, line, what) {
    if (args.length !== n) throw new PxsError(`${what} needs exactly ${n} argument(s)`, line);
  };

  Script.prototype.ensureImage = function (line, what) {
    if (!this.img) throw new PxsError(`${what} needs a canvas first`, line);
  };

  Script.prototype.set = function (x, y, color) {
    const img = this.img;
    if (!img) return;
    const w = img.width, h = img.height;
    if (x < 0 || x >= w || y < 0 || y >= h) return;
    img.pixels[y * w + x] = [color[0], color[1], color[2], color[3]];
  };

  Script.prototype.put = function (x, y, color) {
    this.set(x, y, color);
    if (!this.mirror) return;
    const w = this.img.width, h = this.img.height;
    if (this.mirror === "x" || this.mirror === "xy") this.set(w - 1 - x, y, color);
    if (this.mirror === "y" || this.mirror === "xy") this.set(x, h - 1 - y, color);
    if (this.mirror === "xy") this.set(w - 1 - x, h - 1 - y, color);
  };

  Script.prototype.bboxOutside = function (x0, y0, x1, y1) {
    const w = this.img.width, h = this.img.height;
    return Math.max(x0, x1) < 0 || Math.min(x0, x1) >= w ||
           Math.max(y0, y1) < 0 || Math.min(y0, y1) >= h;
  };

  Script.prototype.targetColor = function (c) { return c !== null ? c : this.color; };

  // commands --------------------------------------------------------------------
  Script.prototype.cmd_canvas = function (node, args, line) {
    this.need(args, 2, line, "canvas");
    const w = this.int(args[0], line, "canvas width");
    const h = this.int(args[1], line, "canvas height");
    if (w < 1 || h < 1) throw new PxsError(`canvas size must be >= 1, got ${w}x${h}`, line);
    this.img = newImage(this.img ? this.img.name : "untitled", w, h);
    this.mirror = null;
    this.canvasSeen = true;
  };

  Script.prototype.cmd_name = function (node, args, line) {
    this.need(args, 1, line, "name");
    if (!SNAKE_RE.test(args[0])) throw new PxsError(`name ${args[0]} is not snake_case`, line);
    if (this.img === null) this.pendingName = args[0];
    else this.img.name = args[0];
  };

  Script.prototype.cmd_frame = function (node, args, line) {
    this.need(args, 2, line, "frame");
    const fw = this.int(args[0], line, "frame width");
    const fh = this.int(args[1], line, "frame height");
    this.ensureImage(line, "frame");
    if (fw < 1 || fh < 1) throw new PxsError(`frame size must be >= 1, got ${fw}x${fh}`, line);
    if (this.img.width % fw !== 0) {
      this.warn(`frame width ${fw} does not evenly divide canvas width ${this.img.width} (line ${line})`);
    }
    if (this.img.height % fh !== 0) {
      this.warn(`frame height ${fh} does not evenly divide canvas height ${this.img.height} (line ${line})`);
    }
    this.img.frameWidth = fw;
    this.img.frameHeight = fh;
  };

  Script.prototype.cmd_bg = function (node, args, line) {
    this.need(args, 1, line, "bg");
    const c = parseColor(args, line).color;
    this.ensureImage(line, "bg");
    for (let y = 0; y < this.img.height; y++)
      for (let x = 0; x < this.img.width; x++) this.set(x, y, c);
  };

  Script.prototype.cmd_color = function (node, args, line) {
    if (args.length === 0) throw new PxsError("color needs a value", line);
    this.color = parseColor(args, line).color;
  };

  Script.prototype.cmd_mirror = function (node, args, line) {
    this.need(args, 1, line, "mirror");
    const m = args[0].toLowerCase();
    if (["x", "y", "xy", "off"].indexOf(m) < 0) {
      throw new PxsError(`mirror mode must be x|y|xy|off, got ${args[0]}`, line);
    }
    this.mirror = m === "off" ? null : m;
  };

  Script.prototype.cmd_shift = function (node, args, line) {
    this.need(args, 2, line, "shift");
    const dx = this.int(args[0], line, "shift dx");
    const dy = this.int(args[1], line, "shift dy");
    this.ensureImage(line, "shift");
    const img = this.img;
    const w = img.width, h = img.height;
    const src = img.pixels.map(p => p.slice());
    const out = Array.from({ length: w * h }, () => [0, 0, 0, 0]);
    for (let y = 0; y < h; y++) {
      for (let x = 0; x < w; x++) {
        const tx = x + dx, ty = y + dy;
        if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
          out[ty * w + tx] = src[y * w + x];
        }
      }
    }
    img.pixels = out;
  };

  Script.prototype.cmd_point = function (node, args, line) {
    this.ensureImage(line, "point");
    const tc = trailingColor(args, line);
    if (tc.rest.length !== 2) throw new PxsError("point needs exactly 2 coordinates", line);
    const x = this.int(tc.rest[0], line, "point x");
    const y = this.int(tc.rest[1], line, "point y");
    const c = this.targetColor(tc.color);
    const w = this.img.width, h = this.img.height;
    if (x < 0 || x >= w || y < 0 || y >= h) {
      this.warn(`point at line ${line} is entirely outside the canvas`);
      return;
    }
    this.put(x, y, c);
  };

  Script.prototype.cmd_line = function (node, args, line) {
    this.ensureImage(line, "line");
    const tc = trailingColor(args, line);
    if (tc.rest.length !== 4) throw new PxsError("line needs 4 coordinates", line);
    const x0 = this.int(tc.rest[0], line, "line coord");
    const y0 = this.int(tc.rest[1], line, "line coord");
    const x1 = this.int(tc.rest[2], line, "line coord");
    const y1 = this.int(tc.rest[3], line, "line coord");
    const c = this.targetColor(tc.color);
    if (this.bboxOutside(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1))) {
      this.warn(`line at line ${line} is entirely outside the canvas`);
    }
    if (x0 === x1 && y0 === y1) { this.put(x0, y0, c); return; }
    const dx = Math.abs(x1 - x0);
    const dy = -Math.abs(y1 - y0);
    const sx = x0 < x1 ? 1 : -1;
    const sy = y0 < y1 ? 1 : -1;
    let err = dx + dy;
    let cx = x0, cy = y0;
    for (;;) {
      this.put(cx, cy, c);
      if (cx === x1 && cy === y1) break;
      const e2 = 2 * err;
      if (e2 >= dy) { err += dy; cx += sx; }
      if (e2 <= dx) { err += dx; cy += sy; }
    }
  };

  Script.prototype.cmd_rect = function (node, args, line) { this.rect(node, args, line, false); };

  Script.prototype.cmd_rectfill = function (node, args, line) { this.rect(node, args, line, true); };

  Script.prototype.rect = function (node, args, line, fill) {
    this.ensureImage(line, "rect");
    const tc = trailingColor(args, line);
    if (tc.rest.length !== 4) throw new PxsError("rect needs x y w h", line);
    const x = this.int(tc.rest[0], line, "rect coord");
    const y = this.int(tc.rest[1], line, "rect coord");
    const w = this.int(tc.rest[2], line, "rect coord");
    const h = this.int(tc.rest[3], line, "rect coord");
    if (w < 0 || h < 0) throw new PxsError(`rect w/h must be >= 0, got ${w}x${h}`, line);
    const c = this.targetColor(tc.color);
    if (this.bboxOutside(x, y, x + w - 1, y + h - 1)) {
      this.warn(`rect at line ${line} is entirely outside the canvas`);
    }
    if (fill) {
      for (let yy = y; yy < y + h; yy++)
        for (let xx = x; xx < x + w; xx++) this.put(xx, yy, c);
      return;
    }
    for (let xx = x; xx < x + w; xx++) {
      this.put(xx, y, c);
      if (h > 1) this.put(xx, y + h - 1, c);
    }
    for (let yy = y; yy < y + h; yy++) {
      this.put(x, yy, c);
      if (w > 1) this.put(x + w - 1, yy, c);
    }
  };

  Script.prototype.cmd_circle = function (node, args, line) { this.circle(node, args, line, false); };

  Script.prototype.cmd_circlefill = function (node, args, line) { this.circle(node, args, line, true); };

  Script.prototype.circle = function (node, args, line, fill) {
    this.ensureImage(line, "circle");
    const tc = trailingColor(args, line);
    if (tc.rest.length !== 3) throw new PxsError("circle needs cx cy r", line);
    const cx = this.int(tc.rest[0], line, "circle coord");
    const cy = this.int(tc.rest[1], line, "circle coord");
    const r = this.int(tc.rest[2], line, "circle coord");
    if (r < 0) throw new PxsError(`circle r must be >= 0, got ${r}`, line);
    const c = this.targetColor(tc.color);
    if (this.bboxOutside(cx - r, cy - r, cx + r, cy + r)) {
      this.warn(`circle at line ${line} is entirely outside the canvas`);
      return;
    }
    const r2 = r * r;
    const inner = fill ? -1 : (r - 1) * (r - 1);
    for (let dy = -r; dy <= r; dy++) {
      for (let dx = -r; dx <= r; dx++) {
        const d2 = dx * dx + dy * dy;
        if (d2 <= r2 && d2 > inner) this.put(cx + dx, cy + dy, c);
      }
    }
  };

  Script.prototype.cmd_fill = function (node, args, line) {
    this.ensureImage(line, "fill");
    const tc = trailingColor(args, line);
    if (tc.rest.length !== 2) throw new PxsError("fill needs x y", line);
    const x = this.int(tc.rest[0], line, "fill x");
    const y = this.int(tc.rest[1], line, "fill y");
    const c = this.targetColor(tc.color);
    const w = this.img.width, h = this.img.height;
    if (x < 0 || x >= w || y < 0 || y >= h) {
      this.warn(`fill at line ${line} is entirely outside the canvas`);
      return;
    }
    const seeds = [[x, y]];
    if (this.mirror === "x" || this.mirror === "xy") seeds.push([w - 1 - x, y]);
    if (this.mirror === "y" || this.mirror === "xy") seeds.push([x, h - 1 - y]);
    if (this.mirror === "xy") seeds.push([w - 1 - x, h - 1 - y]);
    for (const s of seeds) this.flood(s[0], s[1], c);
  };

  Script.prototype.flood = function (x, y, color) {
    const img = this.img;
    const w = img.width, h = img.height;
    const get = (px, py) => img.pixels[py * w + px];
    const target = get(x, y);
    if (target[0] === color[0] && target[1] === color[1] &&
        target[2] === color[2] && target[3] === color[3]) return;
    const isTarget = (px, py) => {
      if (px < 0 || px >= w || py < 0 || py >= h) return false;
      const v = get(px, py);
      return v[0] === target[0] && v[1] === target[1] && v[2] === target[2] && v[3] === target[3];
    };
    const stack = [[x, y]];
    while (stack.length) {
      const s = stack.pop();
      const cx = s[0], cy = s[1];
      if (!isTarget(cx, cy)) continue;
      this.set(cx, cy, color);
      stack.push([cx + 1, cy], [cx - 1, cy], [cx, cy + 1], [cx, cy - 1]);
    }
  };

  Script.prototype.cmd_outline = function (node, args, line) {
    this.ensureImage(line, "outline");
    const lc = leadingColor(args, line);
    const color = lc.color;
    let d = 1;
    const rest = lc.rest;
    if (rest.length > 0) {
      if (rest.length !== 1) throw new PxsError("outline takes [color] [thickness]", line);
      d = this.int(rest[0], line, "outline thickness");
      if (d < 1) throw new PxsError(`outline thickness must be >= 1, got ${d}`, line);
    }
    const c = this.targetColor(color);
    const img = this.img;
    const w = img.width, h = img.height;
    const opaque = new Set();
    for (let y0 = 0; y0 < h; y0++)
      for (let x0 = 0; x0 < w; x0++)
        if (img.pixels[y0 * w + x0][3] !== 0) opaque.add(x0 + "," + y0);
    if (opaque.size === 0) {
      this.warn(`outline at line ${line} found nothing to outline`);
      return;
    }
    for (let ring = 0; ring < d; ring++) {
      const boundary = new Set();
      for (let x0 = 0; x0 < w; x0++) {
        for (let y0 = 0; y0 < h; y0++) {
          const k = x0 + "," + y0;
          if (opaque.has(k)) continue;
          if (opaque.has((x0 - 1) + "," + y0) || opaque.has((x0 + 1) + "," + y0) ||
              opaque.has(x0 + "," + (y0 - 1)) || opaque.has(x0 + "," + (y0 + 1))) {
            boundary.add(k);
          }
        }
      }
      if (boundary.size === 0) break;
      for (const k of boundary) {
        const parts = k.split(",");
        this.set(parseInt(parts[0], 10), parseInt(parts[1], 10), c);
        opaque.add(k);
      }
    }
  };

  Script.prototype.cmd_shade = function (node, args, line) {
    this.ensureImage(line, "shade");
    this.need(args, 1, line, "shade");
    let a = this.int(args[0], line, "shade amount");
    if (a < -255 || a > 255) {
      this.warn(`shade amount ${a} clamped to [-255, 255]`);
      a = Math.max(-255, Math.min(255, a));
    }
    const img = this.img;
    for (let y0 = 0; y0 < img.height; y0++) {
      for (let x0 = 0; x0 < img.width; x0++) {
        const px = img.pixels[y0 * img.width + x0];
        if (px[3] !== 0) {
          px[0] = clampByte(px[0] + a);
          px[1] = clampByte(px[1] + a);
          px[2] = clampByte(px[2] + a);
          img.pixels[y0 * img.width + x0] = px;
        }
      }
    }
  };

  // Public API
  function run(text, img, opts) {
    const warnings = [];
    try {
      const tree = parse(text);
      const s = new Script(tree, warnings);
      const result = s.run(img, !!(opts && opts.requireCanvas));
      return { img: result, warnings: warnings, ok: true };
    } catch (e) {
      if (PxsError.isError(e)) return { error: e, warnings: warnings, ok: false };
      return { error: new PxsError(String(e), null), warnings: warnings, ok: false };
    }
  }

  function scriptHasCanvas(tree) {
    function walk(nodes) {
      for (const n of nodes) {
        if (n.cmd === "canvas") return true;
        if (n.cmd === "repeat" && walk(n.body)) return true;
      }
      return false;
    }
    return walk(tree);
  }

  return {
    run: run,
    parse: parse,
    parseColor: parseColor,
    newImage: newImage,
    PxsError: PxsError,
    scriptHasCanvas: scriptHasCanvas,
    VERSION: "pxs v1"
  };
})();

if (typeof module !== "undefined" && module.exports) module.exports = Pxs;
