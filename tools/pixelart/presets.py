"""Presets for Veiled Pixel Dungeon asset types."""

PRESETS = {
    "tile_16": {
        "width": 16, "height": 16,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "environment/custom_tiles",
        "desc": "Single 16x16 custom tile (environment/custom_tiles/).",
    },
    "tile_16x5": {
        "width": 80, "height": 16,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "environment/custom_tiles",
        "desc": "5-wide tile strip (e.g. blood_altar_floor is 80x80; use for wide floors).",
    },
    "tile_80": {
        "width": 80, "height": 80,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "environment/custom_tiles",
        "desc": "5x5 tile floor (80x80, e.g. blood_altar_floor).",
    },
    "tile_128": {
        "width": 128, "height": 128,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "environment/custom_tiles",
        "desc": "8x8 tile block (128x128).",
    },
    "tile_256": {
        "width": 256, "height": 256,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "environment/custom_tiles",
        "desc": "16x16 tile block (256x256).",
    },
    "mob_strip": {
        "width": 12, "height": 15,
        "frameWidth": 12, "frameHeight": 15,
        "mode": "rgba",
        "target_dir": "sprites",
        "desc": "Single mob frame (12x15). For strips set width=12*N.",
    },
    "mob_strip_4": {
        "width": 48, "height": 15,
        "frameWidth": 12, "frameHeight": 15,
        "mode": "rgba",
        "target_dir": "sprites",
        "desc": "4-frame mob strip (48x15).",
    },
    "mob_strip_6": {
        "width": 72, "height": 15,
        "frameWidth": 12, "frameHeight": 15,
        "mode": "rgba",
        "target_dir": "sprites",
        "desc": "6-frame mob strip (72x15).",
    },
    "item_icon": {
        "width": 16, "height": 16,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "sprites",
        "desc": "16x16 item icon (items.png / item_icons.png slot).",
    },
    "effect_strip": {
        "width": 32, "height": 16,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "effects",
        "desc": "2-frame effect strip (32x16) for effects/.",
    },
    "fx_16": {
        "width": 16, "height": 16,
        "frameWidth": 16, "frameHeight": 16,
        "mode": "rgba",
        "target_dir": "effects",
        "desc": "Single 16x16 effect icon.",
    },
}

ASSET_TYPES = {
    "tile": {"multiple_of": (16, 16)},
    "mob": {"frame": (12, 15)},
    "item": {"size": (16, 16)},
    "effect": {"multiple_of": (8, 8)},
}


def preset_names():
    return sorted(PRESETS.keys())


def get_preset(name):
    if name not in PRESETS:
        raise KeyError(f"unknown preset {name!r}; choose from {preset_names()}")
    return PRESETS[name]
