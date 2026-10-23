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
  "pixels": [[r,g,b,a], ...] }
```

Pixels are flat, row-major, values 0-255. `pixels` may also be a 2D array of
rows; both are accepted.

## Web editor (humans)

Open `tools/pixelart/editor.html` in a browser. Preset -> Apply size -> draw
(pencil/eraser/eyedropper/fill) -> Save JSON / Export PNG / Promote. Promote uses
the File System Access API to write both files to a chosen folder; falls back to
download if unavailable.

## Agent CLI (`px.py`)

Run from `tools/pixelart/` with `PYTHONPATH=.`:

```bash
./px.py new <name> --preset tile_16 --out /tmp/opencode/pixelart-staging/<name>.px.json
./px.py render <file.px.json> [--out out.png]
./px.py import <file.png> [--preset ...] [--out out.px.json]
./px.py validate <file> [--type tile|mob|item|effect] [--assets-java <path>]
./px.py promote <file> [--preset ...] [--target <subdir>] [--force]
./px.py palette <file.png> [--format text|json]
./px.py variants <base.px.json> --count N --out-dir <dir>
./px.py info
```

Default staging dir: `/tmp/opencode/pixelart-staging/` (override `--staging`).

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
