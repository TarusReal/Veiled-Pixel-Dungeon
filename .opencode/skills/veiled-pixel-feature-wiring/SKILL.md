---
name: Veiled Pixel Feature Wiring
description: >-
  Technical rules for Veiled Pixel Dungeon (LibGDX fork of Shattered Pixel Dungeon):
  creating pixel art assets (tiles, sprites) that compile and render correctly, and wiring
  new features (events, rooms, NPCs, items, UI) into the existing architecture with EN/DE
  bundles, save/load safety, and tests. Use when adding or changing art assets, custom
  tiles, sprite animations, message bundles, EventRoom/DungeonEvent content, Hero stats,
  or when the build needs the pinned JDK.
---

# Veiled Pixel Dungeon: Art + Feature Wiring

Technical, condensed. Verify everything with `./gradlew :core:test -Dorg.gradle.java.home=/home/atrebas/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2` (the JDK flag is mandatory; newer JDKs fail).

## Pixel art

- Assets live in `core/src/main/assets/environment/custom_tiles/` (tiles) and `core/src/main/assets/sprites/` (characters). Reference them through constants in `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java` (`Assets.Environment.*`, `Assets.Sprites.*`); no string literals elsewhere.
- Tile visuals: tile edge = 16 px (`DungeonTilemap.SIZE`). Texture width must be a multiple of 16; `CustomTilemap.create()` maps tiles via `mapSimpleImage(txX, txY, TEX_WIDTH)`.
- Bottom-anchored objects (altars, gravestones): keep the artwork complete and full-canvas, then nudge with `v.y -= <px>` inside the `Tilemap.create()` override. Do not crop pixels off the texture to fake the offset.
- Characters: `MobSprite` frames are 12x15 px (`new TextureFilm(texture, 12, 15)`), texture width = multiple of 12. `idle` must be looping (`new Animation(fps, true)`); reference every usable frame in `idle` - unlisted frames never play. Match frame indexes to the PNG strip order.
- Radial style rules: 3-4 tones per ramp, no anti-aliasing or blur, outlines consistent with the surrounding SPD art, palette close to existing tiles so rooms do not clash.
- Generation workflow: spawn a subagent for the pixel art draft; verify with pixel metrics (PNG size, bbox, palette) - never trust verbal art claims, re-read the PNG programmatically if in doubt.
- Examinable tiles: `CustomTilemap.name(x,y)` / `desc(x,y)` are used by `WndInfoCell` only for cells where `image(x,y)` is non-null. For overlapping visuals (floor under an object), return `null` from the larger tilemap's `image()` at the covered cell so the object's name/desc wins. Bundle keys: fully qualified class name, lowercased, nested classes use `$` (e.g. `levels.rooms.special.eventroom$bloodaltarvis.name`), both EN + `_de` file.

## Feature wiring

- Read `docs/guidelines/Implementation-Guidelines/` before touching a subsystem (e.g. `creature-guideline.md` for mobs). Keep changes structurally identical to the neighbouring code.
- Strings: every player-visible text goes into `core/src/main/assets/messages/**` + `*_de.properties` as EN/DE pair. Key lookup is class-based (`Messages.get(this, "key")` -> package lowercase). Never ship a key in only one language; assert with `assertNotEquals(Messages.NO_TEXT_FOUND, ...)`.
- Save/load: persist runtime state in `storeInBundle`/`restoreFromBundle` with `final String` key constants. Read new fields with `bundle.getBoolean`/`getInt` (default false/0) so old saves load without crashes. Hero stats go in `actors/hero/Hero.java` (`theoreticalHT()`, `updateHT()`, `HP = Math.min(HP, HT)`).
- Events: `events/DungeonEvent` subclasses with unique `key()`; lifecycle `canAppear()` -> `choices()/label()` -> `apply()` (see `docs/guidelines/.../event-guideline.md` for the full walkthrough with the Blood Altar as worked example).
- NPCs/mobs: sprite in `sprites/`, register class in `Bestiary` (existing category lists, e.g. `NEUTRAL`) and add `actors.<name>.name/desc/discover_hint` keys; NPC specialization extends `EventSpirit` for event carriers.
- UI dialogs: follow the Wandmaker pattern (`WndXXX extends WndCompactOptions`, result confirmed via `WndQuest`, close effect via `ElmoParticle.FACTORY` like Shopkeeper). Keep button text Small. Show windows only on the render thread (`GameScene.show`).
- Level placement: special rooms extend `rooms.special.SpecialRoom` (min 7x7); share construction helpers as `public static` methods so the debug/test level (`Region0Level`, depth 0) and the special room reuse the same geometry; expose `public static` inner classes (tilemap visuals are restored from bundles via reflection).
- Tests: add JUnit tests extending `test/GameTestBase` when gameplay logic changes (tests run serially; global state is reset per test). EventSmokeTest covers room/event invariants; assert both EN and DE bundle resolution.
- Do not touch `CLAUDE.md`, `Agent.md` or unrelated files while wiring a feature; keep diffs scoped to subsystem + assets + bundles + tests.
