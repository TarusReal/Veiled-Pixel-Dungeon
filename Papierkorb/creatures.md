# Guideline: neues Lebewesen implementieren

## Ziel
Ein neues Lebewesen muss im Spiel vollständig integriert sein: sichtbar, spielbar, registriert, spawnbar und savegame-sicher. Ein Monster oder NPC darf nicht nur existieren, sondern muss auch im Spielfluss, im Journal und im Savegame korrekt funktionieren.

## Required checklist

### 1. Klasse und Verhalten
- Die neue Klasse korrekt im passenden Bereich anlegen, z. B. unter `actors/` oder einem passenden Unterpaket.
- Die passende Basisklasse verwenden: `Mob`, `Hero`, `Char` oder eine spezialisierte Unterklasse.
- AI, Stats, HP, Angriff, Verteidigung, Spezialfähigkeiten und Verhalten sauber definieren.
- Auf Randfälle prüfen: Tod, Status-Effekte, Kampf, Interaktion, Verhalten in Leveln.

### 2. Darstellung
- Sprite, Animation oder Visuals entsprechend registrieren.
- Die korrekte Darstellung im passenden Sprite-/Actor-System einbinden.
- Wenn das Lebewesen mehrere Varianten oder Zustände hat, diese ebenfalls sauber abbilden.

### 3. Texte und UI
- Namen und Beschreibungen in den `.properties`-Dateien ergänzen.
- Wenn besondere Status-Effekte, Dialoge oder Fähigkeiten existieren, diese ebenfalls mit Texten versehen.
- Keine hardcodierten Strings im Spielcode verwenden.

### 4. Bestiary / Journal / Registrierung
- Falls das Lebewesen in Bestiary, Journal, Katalog oder ähnlichen Registerlisten erscheinen soll, dort registrieren.
- Das ist ein kritischer Teil: Ein Monster kann technisch funktionieren, aber für den Spieler „unsichtbar“ sein, wenn es nicht registriert wurde.

### 5. Spawn und Level-Integration
- Das Lebewesen in die passenden Spawn-Tabellen, Level- oder Mob-Registry-Einträge einbauen.
- Tiefe, Bracket, Spawn-Wahrscheinlichkeit und passende Region / Umgebung prüfen.
- Ein Monster, das nie spawnt, ist für den Spielablauf nicht nutzbar.

### 6. Loot und Drops
- Wenn das Lebewesen Loot oder besondere Drops hat, die passende Drop- und Generator-Logik ergänzen.
- Dargestellte Drops müssen mit den tatsächlichen Loot-Tabellen übereinstimmen.

### 7. Savegame / Persistenz / Migration
- Wenn das Lebewesen oder zugehörige Zustände gespeichert werden, die Persistenz korrekt prüfen.
- Jede Enum-Konstante oder serialisierte Referenz muss bei Umbenennungen / Änderungen mit Migration oder Alias bedacht werden.
- Bei Klassen-/Enum-Umbenennungen: `Bundle.addAlias(...)` oder vergleichbare Migrationslogik ergänzen.

### 8. Balance und Spielkontext
- Stärke, HP, Schadenswerte, Spawn-Häufigkeit und Loot müssen zum vorhandenen Spielniveau passen.
- Ein neues Lebewesen darf nicht zufällig unspielbar, unfair oder reproduzierbar fehlerhaft werden.

### 9. Validation (für den User)
- Das Lebewesen im Spiel testen:
  - erscheint es im passenden Level?
  - trifft es richtig?
  - zeigt es das richtige Sprite?
  - erscheint es im Bestiary/Journal?
  - dropt es korrekt?
  - kann es gespeichert/geladen werden?

## Absolute Vermeidung
- Keine „verborgenen“ Kreaturen: Klasse existiert, aber kein Spawn-, kein Bestiary-, kein Text- und kein Sprite-Setup.
- Keine Enum-/Klassennamen-Änderungen ohne Migrations-/Alias-Plan.
- Keine neuen serialisierten Werte ohne Savegame-Check.
- Keine Spawn-Listen ohne korrekte Tiefe / Bracket / Region.

## Definition of Done
Ein neues Lebewesen gilt als fertig, wenn all das erfüllt ist:
- Klasse implementiert
- Sprite/Visuals registriert
- Name/Description ergänzt
- Bestiary/Journal registriert
- Spawn-Logik eingebaut
- Loot/Drops prüfen
- Savegame-/Persistenz-Sicherheit geprüft
- Funktionstest durchgeführt

## Kurz-Checkliste
- [ ] Klasse erstellt
- [ ] Sprite registriert
- [ ] Texte ergänzt
- [ ] Bestiary/Journal registriert
- [ ] Spawn eingebaut
- [ ] Loot/Drop geprüft
- [ ] Savegame/Bundles geprüft
- [ ] Balance/Funktionstest durchgeführt
