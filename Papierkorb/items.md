# Guideline: neues Item implementieren

## Ziel
Ein neues Item muss im Spiel vollständig nutzbar, sichtbar und savegame-sicher sein. Es darf nicht nur im Code existieren, sondern muss auch richtig erzeugt, dargestellt, registriert und geladen werden.

## Required checklist

### 1. Klasse und Logik
- Die Item-Klasse korrekt im passenden Paket anlegen, z. B. unter `items/` oder einem passenden Unterpaket.
- Die richtige Basisklasse verwenden: `Item`, `Weapon`, `Armor`, `Potion`, `Scroll`, `Ring`, `Wand`, etc.
- Wenn das Item Verhalten hat, auch den Effekt/Use-Flow sauber implementieren.
- Auf Null-/Edge-Cases prüfen: Unbekannte Zustände, Stack-Verhalten, Item-IDs, Nutzung im Inventar.

### 2. Darstellung
- Ein Sprite oder Atlas-Eintrag registrieren.
- Die korrekte Grafikkonfiguration im `ItemSpriteSheet` oder vergleichbaren Systemen anpassen.
- Wenn das Item visuell mit Farben, Varianten oder Animationen arbeitet, diese ebenfalls sauber registrieren.

### 3. Texte und sprachliche Einträge
- Name und Beschreibung in den passenden `.properties`-Dateien ergänzen.
- Keine hardcodierten Texte im Code verwenden.
- Wenn der Text-Keys automatisch aus dem Klassennamen erzeugt wird, die Klasse/Datei entsprechend konsistent benennen.

### 4. Generator / Loot / Spawn
- Falls das Item zufällig auftauchen soll, in `Generator` bzw. den passenden Loot-Tabellen registrieren.
- Kategorie, Gewicht, Tier und Spawn-Wahrscheinlichkeiten richtig setzen.
- Wenn ein neuer Item-Typ introduced wird, nicht nur die Klasse anlegen, sondern auch die Erzeugung und Verteilung prüfen.

### 5. Journal / Katalog / Register
- Wenn das Item im Katalog, Catalog, Journal oder Inventar auftauchen soll, dort registrieren.
- Regel: Wenn ein neues Item für Spieler sichtbar sein soll, muss es irgendwo als registriertes Objekt in der UI/Journal-Welt auftauchen.

### 6. Crafting / Recipes / Interaktionen
- Falls das Item Teil eines Rezeptsystems, Handelse oder Kombinationen ist, die passenden Recipe- oder Interaktionspunkte ergänzen.
- Nicht nur die Klasse anlegen, sondern auch alle relevanten Herstellungs-/Verbrauchswege testen.

### 7. Savegame / Bundles / Persistenz
- Wenn das Item serialisiert wird oder als `Bundlable` funktioniert, die Savegame-Registrierung prüfen.
- Bei Klassenumbenennungen oder Verschiebungen immer `Bundle.addAlias(...)` ergänzen.
- Alte Namen bzw. Migrationspfade nicht vergessen, wenn ein Savegame-Format geändert wurde.
- Jede Enum-Konstante oder gespeicherte Wert-Referenz muss mit Migrationssicht beachten werden.

### 8. Validation (für den User)
- Das Item in der Praxis testen:
  - erscheint es im Inventar?
  - kann es aufgerufen/benutzt werden?
  - hat es richtige Texte?
  - kann es als Loot erscheinen?
  - kann es gespeichert und geladen werden?

## Absolute Vermeidung
- Keine „unsichtbaren“ Items: Klasse existiert, aber kein Sprite, kein Generator-Eintrag, kein Text.
- Keine neuen Bundlable-Klassen ohne Savegame-Check.
- Keine Umbenennung von Klassen/Enums ohne Migration oder Alias.
- Keine Änderungen an gespeicherten Werten „nebenbei“, ohne die Ladesicherheit zu prüfen.

## Definition of Done
Ein neues Item gilt als fertig, wenn all das erfüllt ist:
- Klasse implementiert
- Sprite/Darstellung registriert
- Texte ergänzt
- Generator/Loot/Spawn eingebaut
- Katalog/Journal registriert, falls nötig
- Savegame/Bundles geprüft
- Validierung durchgeführt

## Kurz-Checkliste
- [ ] Klasse erstellt
- [ ] Sprite registriert
- [ ] Texte ergänzt
- [ ] Generator/Loot eingebaut
- [ ] Catalog/Journal registriert
- [ ] Savegame/Bundles geprüft
- [ ] Funktionstest durchgeführt
