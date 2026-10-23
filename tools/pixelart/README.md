# Veiled Pixel Art Studio

Tool for creating pixel-art assets for Veiled Pixel Dungeon, usable by both
the coding agent and humans. Shared source format: `.px.json` (JSON pixel grid).
PNG is the rendered output.

## Layout

```
tools/pixelart/
  px.py            CLI for the agent
  pixelart.py      importable Python module (draw helpers)
  editor.html      single-file web editor for humans
  presets.py       size/type presets and conventions
  validate.py      validation rules
  promote.py       staging -> project assets
  schema.json      JSON schema for .px.json
  palettes/        reference palettes (GIMP .gpl) extracted from existing assets
```

## Shared format (`.px.json`)

```json
{
  "name": "blood_altar",
  "width": 16,
  "height": 16,
  "frameWidth": 16,
  "frameHeight": 16,
  "mode": "rgba",
  "pixels": [[[r,g,b,a], ...], ...]
}
```

Pixels are stored row-major. `pixels` may be a flat list or a 2D array
(rows); both are accepted. Values are 0-255.

## Conventions

| Type   | Size                       | Target dir                          |
|--------|----------------------------|-------------------------------------|
| tile   | width/height multiples of 16 | `environment/custom_tiles/`       |
| mob    | 12 x 15 per frame, strip width = 12*N | `sprites/`              |
| item   | 16 x 16                    | `sprites/`                          |
| effect | multiples of 8             | `effects/`                          |

Filenames: `snake_case.png`.

## Workflows

### Human (web editor)

Open `editor.html` in a browser. Choose a preset, draw with pencil/eraser/
eyedropper/fill, set color + alpha. Save JSON (`.px.json`) and/or export PNG.
**Promote** button writes both files to a chosen folder (File System Access API)
or downloads them if unavailable.

### Agent (CLI)

```bash
cd tools/pixelart
export PYTHONPATH=.

# create blank image
./px.py new my_tile --preset tile_16 --out /tmp/opencode/pixelart-staging/my_tile.px.json

# inspect presets
./px.py info

# render JSON -> PNG
./px.py render /tmp/opencode/pixelart-staging/my_tile.px.json

# import existing PNG -> JSON
./px.py import core/src/main/assets/environment/custom_tiles/blood_altar.png --preset tile_16

# validate
./px.py validate /tmp/opencode/pixelart-staging/my_tile.px.json --type tile

# extract palette
./px.py palette some.png

# create 3 variants for the user to choose from
./px.py variants base.px.json --count 3 --out-dir /tmp/opencode/pixelart-staging/vs

# promote (validate + copy into project assets)
./px.py promote /tmp/opencode/pixelart-staging/my_tile.px.json --preset tile_16
./px.py promote ... --force   # overwrite existing
```

### Agent (programmatic)

```python
import sys; sys.path.insert(0, 'tools/pixelart')
import pixelart as P

img = P.new_image('my_tile', 16, 16)
P.rect(img, 2, 2, 12, 12, (200, 100, 50, 255))
P.set_pixel(img, 7, 7, (255, 255, 255, 255))
P.save_json(img, '/tmp/opencode/pixelart-staging/my_tile.px.json')
P.render(img, '/tmp/opencode/pixelart-staging/my_tile.png')
```

## Staging & promotion

Default staging dir: `/tmp/opencode/pixelart-staging/`. Override with `--staging`.

Promotion validates first (size, mode, filename). On success it copies the PNG into
`core/src/main/assets/<target>/` and prints a reminder to add a constant to
`Assets.java` (no automatic edit, to avoid breaking code). Use `--force` to
overwrite an existing destination.

## Validation

`validate.py` checks: required fields, dimensions, pixel count/values, dimension
rules per asset type, snake_case filename. `--assets-java` adds a warning if no
constant references the file.

## Notes

- Free colors (RGBA). No palette enforcement; reference palettes provided for
  consistency with existing SPD art.
- Static images only (no animation playback). Frame strips are wide PNGs.
- `CLAUDE.md` and `Agent.md` are intentionally left untouched.
PYEOF
echo done