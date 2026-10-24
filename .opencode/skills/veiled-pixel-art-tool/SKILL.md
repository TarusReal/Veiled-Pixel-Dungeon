---
name: Veiled Pixel Art Tool
description: >-
  Use the project's pixel-art tooling (tools/pixelart/) to create, edit, validate, and
  promote pixel-art assets for Veiled Pixel Dungeon. Covers the shared .px.json format,
  the web editor (editor.html), the agent CLI (px.py), the Python module (pixelart.py),
  presets, validation rules, staging/promotion workflow, and the 2-5 variant draft
  workflow. Use whenever the task is to create or modify a pixel-art asset (custom tile,
  mob sprite, item icon, effect), import an existing PNG, or promote a staging asset
  into core/src/main/assets.
---

# Veiled Pixel Art Tool

Tooling lives in `tools/pixelart/`. Shared source format is `.px.json` (JSON pixel
grid); PNG is the rendered output. Use it instead of hand-writing PNG bytes.

## Conventions

| Type   | Size                              | Target dir                        |
|--------|-----------------------------------|-----------------------------------|
| tile   | width/height multiples of 16      | `environment/custom_tiles/`       |
| mob    | 12 x 15 per frame, strip = 12*N   | `sprites/`                        |
| item   | 16 x 16                           | `sprites/`                        |
| effect | multiples of 8                    | `effects/`                        |

Filenames: `snake_case.png`. Reference assets only via constants in
`core/.../shatteredpixeldungeon/Assets.java` (`Assets.Environment.*`,
`Assets.Sprites.*`); no string literals elsewhere.

## Shared format (`.px.json`)

```json
{ "name": "blood_altar", "width": 16, "height": 16,
  "frameWidth": 16, "frameHeight": 16, "mode": "rgba",
  "pixels": [[r,g,b,a], ...],
  "recipe": "canvas 16 16\npoint 3 4 ffffff\n..." }
```

Pixels are per-pixel lists, row-major, values 0-255. `pixels` may also be a
2D array of rows or a flat channel array; both are accepted. `recipe` is
optional and holds the pxs script text that produced the image (see below);
the web editor can re-run it via Recompile.

## pxs scripts (`.pxs`)

Line-based mini-language for agent-drawn assets (canonical interpreter:
`dsl.py`, JS port for the editor: `pxscript.js`). `#` starts a comment.

Header: `canvas W H` (mandatory when fresh), `name <snake_case>`, `frame W H`,
`bg <color>`. Colors: `3a5f2a`, `3a5f2a80` (hex without `#`), `rgb r g b[a]`.

Commands:

```
color <color>            # set current color
point x y [color]
line x1 y1 x2 y2 [color]
rect x y w h [color]     # outline
rectfill x y w h [color]
circle cx cy r [color]
circlefill cx cy r [color]
fill x y [color]         # flood fill on exact RGBA match
outline [color] [thickness]   # outside, 4-neighbour; default current color, 1px
shade amount             # add amount to RGB of non-transparent pixels
mirror x|y|xy|off        # symmetry for subsequent draw commands
shift dx dy              # move whole image, transparent fill
repeat N {               # multiline block ending with a lone }
```

Out-of-bounds pixels are clipped; a shape completely outside adds a warning
(not an error). Full grammar and behavior: docstring in `dsl.py`. The same
interpreter is embedded in the editor (Recipe textarea + Recompile).

## Agent CLI (`px.py`)

Run from `tools/pixelart/`.

```bash
./px.py new <name> --preset tile_16 --out /tmp/opencode/pixelart-staging/<name>.px.json
./px.py render <file.px.json> [--out out.png]
./px.py import <file.png> [--preset ...] [--out out.px.json]
./px.py compile <file.pxs> [--out out.px.json] [--png out.png] [--dry-run] [--preview]
./px.py draw <target.px.json> "cmd ..." [...] [--png out.png] [--dry-run] [--preview]
./px.py preview <file.pxs|.px.json|.png>
./px.py selftest
./px.py validate <file> [--type tile|mob|item|effect] [--assets-java <path>]
./px.py promote <file> [--preset ...] [--target <subdir>] [--force]
./px.py palette <file.png> [--format text|json]
./px.py variants <base.px.json> --count N --out-dir <dir>
./px.py info
```

- `compile` writes recipe = script text into the `.px.json`.
- `draw` appends commands to an existing target (re-runs prior recipe); a
  fresh target needs a `canvas` command. `--dry-run` validates without writing.
- `selftest` compares `dsl.py` against the golden cases in `tests/cases/`.
- `validate` returns nonzero for hard errors only; warnings go to stdout.

Default staging dir: `/tmp/opencode/pixelart-staging/` (override `--staging`).

## Tests (`tests/`)

- `cases/*.pxs` + `*.expected.json`: golden cases (pixels + warnings).
- `gen_expected.py`: regenerates the `.expected.json` from `dsl.py` after a
  deliberate behavior change.
- `run_py.py` / `run_js.js`: full comparison runner for Python and the JS
  port; both must produce identical pixels and warnings.

## Web editor (humans)

Open `tools/pixelart/editor.html` in a browser (keep `pxscript.js` next to it).
Presets and manual drawing: pencil/eraser/eyedropper/fill, Save JSON / Export
PNG / Promote. The Recipe (pxs) panel holds the script text; Recompile re-runs
it (with overwrite confirmation, Ctrl+Z undo when the size does not change).
Promote uses the File System Access API to write both files to a chosen
folder; falls back to download if unavailable.

## Agent module (`pixelart.py`)

```python
import sys; sys.path.insert(0, 'tools/pixelart')
import pixelart as P
img = P.new_image('name', 16, 16)
P.rect(img, 2, 2, 12, 12, (200, 100, 50, 255))
P.set_pixel(img, 7, 7, (255, 255, 255, 255))
P.save_json(img, '/tmp/opencode/pixelart-staging/name.px.json')
P.render(img, '/tmp/opencode/pixelart-staging/name.png')
```

Helpers: `set_pixel`, `get_pixel`, `rect`, `line`, `flood_fill`, `flip_h`,
`flip_v`, `rotate_90`, `scale_nearest`, `unique_colors`, `load_json`,
`save_json`, `render`, `import_png`.

## Validation

`validate.py` checks required fields, dimensions, pixel count/values, dimension
rules per type, snake_case filename. With `--assets-java` it warns if no constant
references the file. Warnings (prefixed `WARNING:`/`NOTE:`) do not fail
validation; hard errors do.

## Promotion workflow

1. Validate first.
2. Copy PNG into `core/src/main/assets/<target>/`.
3. Print a reminder to add a constant to `Assets.java` (no automatic edit).
4. `--force` overwrites an existing destination.

## Draft workflow (per project skill)

Spawn 2-5 variants in `/tmp` via `variants`, let the user pick one, then
`promote`. Inform the user of the other variants in case they want to switch.

## Bottom-anchored tiles

Keep artwork full-canvas; nudge with `v.y -= <px>` in the `Tilemap.create()`
override. Do not crop pixels to fake the offset.

## Related

- `docs/guidelines/Implementation-Guidelines/` for wiring (bundles, registries).
- `docs/pixelart-tool.md` if present.
