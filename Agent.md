# Open Pixel Dungeon

Fork von [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon),
das wiederum auf Watabous Pixel Dungeon basiert. Java + libGDX, GPLv3.
Ziel: eine saubere, erweiterbare Basis für eigene Inhalte.

## Quick start

Gradle-Wrapper verwenden (`gradlew.bat` unter Windows, `./gradlew` sonst):

```bash
gradlew desktop:debug        # Spiel starten (Entwicklung)
gradlew desktop:release      # ausführbares Jar bauen
gradlew android:assembleDebug
gradlew build                # alles kompilieren
```

Tests laufen headless
`docs/testing.md`:

```bash
gradlew :core:test
```

Java 11-Quellkompatibilität gilt über `appJavaCompatibility` in `build.gradle`.
Der Desktop-Release lädt bei Bedarf ein JDK 17 herunter.

## Modulübersicht

| Modul | Zweck                                                                                                                   |
| --- |-------------------------------------------------------------------------------------------------------------------------|
| `core/` | Hauptspielcode; hier                                                                                                    |
| `SPD-classes/` | Watabous Engine-Schicht: Rendering (`noosa`, `glwrap`, `Bundle`, `Random`, `PathFinder`); möglichst unverändert lassen. |
| `android/`, `desktop/`, `ios/` | Launcher und Plattform-Support.                                                                                         |
| `services/` | Update-Check und News-Feed; bei Forks oft auf `debug`-Varianten umstellen.                                              |
| `core/src/main/assets/` | Grafiken, Sounds und `messages/`- alle Texte.                                                                           |

Wichtige Pakete:

- `Dungeon.java`: globaler Spielzustand statisch (Held, Level, Tiefe, Seed, Speichern/Laden).
- `actors/`: `Char` → `Hero` / `Mob`; `buffs/` (Statuseffekte) und `hero/Talent.java`.
- `items/`: alle Gegenstände; `Generator.java` ist die zentrale Loot-Tabelle.
- `levels/` – Levelklassen pro Region, `rooms/` (Raumtypen), `builders/`,
  `painters/`, `traps/`, `Terrain.java` (Kachel-IDs). `RegionDefinition.java`
  zentrale Regionen-Tabelle (Etage 1–26, s. Fallstrick #7);
  `Region0Level.java` für Etage 0
  ("Oberfläche", noch ohne Inhalt – siehe `docs/depth0-implementation.md`).
- `scenes/`: Bildschirme; `GameScene.java` ist der Spielbildschirm (statisch)
- `journal/`: `Bestiary`, `Catalog`, `Document` für Content-Registrierung.
- `sprites/`: `ItemSpriteSheet.java` verbindet Items mit Sprite-Atlassen.

## Wichtige Projektregeln

### 1) Savegames sind empfindlich auf Umbenennungen

- Klassen- und Enum-Namen stehen im Savegame.
- Eine Umbenennung kann Objekte still verschwinden lassen oder Werte auf Standardwerte zurücksetzen.
- `Bundle.addAlias(Class, String)` ist der richtige Migrationsweg.
- Für Umbenennungen an `Bundlable`-Klassen oder gespeicherten Enums immer auch die entsprechende Aliase-/Migrationslogik ergänzen.
- Übersetzungs-Keys basieren auf Klassennamen (`Messages.get(this, "name")`); `.properties`-Dateien müssen dann mitgezogen werden.

### 2) `Generator` und ähnliche Datenstrukturen sind fehleranfällig

- `Generator.Category` darf nicht über getrennte, manuell parallel gepflegte Arrays und Gewichte verwaltet werden.
- Beim Ändern von `Generator`, `StandardRoom.chances` oder ähnlichen Strukturen: Länge und Reihenfolge beider Seiten immer prüfen.
- `Generator.random()` ist nur innerhalb eines geseedeten RNG-Blocks reproduzierbar.
- `Dungeon.init()` setzt den Seed-Zufall nach `Generator.fullReset()` wieder zurück; reproduzierbares Loot entsteht erst mit einem eigenen Seed push im Level-Setup.

### 3) Ebenen-/Regionslogik ist zentralisiert

- Ebenen 1–26 werden über `RegionDefinition[]` verwaltet.
- `Dungeon.newLevel()`, `Dungeon.bossLevel()` und `RegularLevel` nutzen diese zentrale Tabelle statt eigener `switch(depth)`-Blöcke.
- Tiefe 0 ist eine eigene Sonderregion (`Region0Level`, `RegionDefinition.REGIONS[0]`), noch nicht vollständig belegt.
- `Terrain`-IDs, `-1`-Sentinels für "kein Wert" und ähnliche Savegame-ähnliche Konstanten müssen konsistent bleiben; `0` ist für "keine Tiefe" nicht mehr erlaubt.

### 4) Neue Inhalte müssen an vielen Stellen registriert werden

Ein neues Item/Content braucht typischerweise:

- die Java-Klasse,
- Eintrag in `Generator`,
- Sprite-Registrierung,
- `journal/Catalog`/`Bestiary`/`Document`,
- ggf. Rezept/Item-Logik,
- passende Texte in den `.properties`-Dateien.

Das wird durch den Compiler nicht erzwungen; die Registrierung muss manuell erfolgen.

## Arbeitsregeln

- Umbenennung und Logikänderung niemals im selben Commit kombinieren.
- Spielbalance nur auf ausdrückliche Anweisung ändern.
- Vor einer Umbenennung prüfen: `Bundlable`? gespeicherte Enum-Konstante? `messages/`-Einträge?
- Details und größere Umbauvorhaben in `docs/` schreiben, nicht in diese Datei.
- Dokumentationsbeispiele nur dann mit Sprach-Tag versehen, wenn sie compilierbar sind; bei Pseudocode/Fragmenten ohne Sprach-Tag bleiben.

## Kurz gesagt

Das Projekt ist ein Java/libGDX-Open-Source-Roguelike-Fork mit starker Savegame-/Content-Ökonomie: Das größte Risiko liegt nicht im Gameplay-Code, sondern in serialisierungs- und registrierungssensitiven Stellen wie `Bundle`, `Generator`, `RegionDefinition` und den Übersetzungs-/Enum-Keys. Änderungen dort müssen bewusst und migrationssicher erfolgen.
