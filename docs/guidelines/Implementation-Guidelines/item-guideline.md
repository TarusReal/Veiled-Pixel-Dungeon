# Guideline: Item

## Ziel
Ein neues Item muss im Spiel vollständig nutzbar, sichtbar und savegame-sicher sein. Es darf nicht nur im Code existieren, sondern muss auch richtig erzeugt, dargestellt, registriert und geladen werden.

## Ausführliche Checklist

### 1. Klasse und Logik
- Die Item-Klasse korrekt im passenden Paket anlegen, z. B. unter `items/` oder einem passenden Unterpaket.
- Die richtige Basisklasse verwenden: `Item`, `Weapon`, `Armor`, `Potion`, `Scroll`, `Ring`, `Wand` usw.
- Wenn das Item Verhalten hat, den Effekt- und Use-Flow sauber implementieren.
- Auf Null-/Edge-Cases prüfen: Stack-Verhalten, Statuszustände, Nutzung im Inventar, Sonderfälle bei Aktivierung.

### 2. Darstellung
- Ein Sprite oder Atlas-Eintrag registrieren.
- Die korrekte Grafik-Konfiguration in `ItemSpriteSheet` oder vergleichbaren Systemen anpassen.
- Wenn das Item visuell mit Farben, Varianten oder Animationen arbeitet, diese ebenfalls sauber registrieren.

### 3. Texte und sprachliche Einträge
- Name und Beschreibung in den passenden `.properties`-Dateien ergänzen.
- Keine hardcodierten Texte im Code verwenden.
- Wenn Texte automatisch aus dem Klassennamen abgeleitet werden, die Klasse und der Name konsistent halten.

### 4. Generator / Loot / Spawn
- Falls das Item zufällig auftauchen soll, in `Generator` oder den passenden Loot-Tabellen registrieren.
- Kategorie, Gewicht, Tier und Spawn-Wahrscheinlichkeiten richtig setzen.
- Nicht nur die Klasse anlegen, sondern auch die Erzeugung und Verteilung prüfen.

### 5. Journal / Katalog / Register
- Wenn das Item für Spieler sichtbar sein soll, dort registrieren, wo es in UI, Katalog, Journal oder Inventar auftaucht.
- Neue Inhalte müssen nicht nur vorhanden, sondern auch auffindbar und nachvollziehbar sein.

### 6. Crafting / Recipes / Interaktionen
- Falls das Item Teil eines Rezeptsystems, Handels oder Kombinationen ist, die passenden Recipe- oder Interaktionspunkte ergänzen.
- Nicht nur die Klasse anlegen, sondern auch alle relevanten Herstellungs- und Verbrauchswege berücksichtigen.

### 7. Savegame / Bundles / Persistenz
- Wenn das Item serialisiert wird oder als `Bundlable` funktioniert, die Savegame-Registrierung prüfen.
- Bei Klassenumbenennungen oder -verschiebungen immer `Bundle.addAlias(...)` ergänzen.
- Keine Enum-/Wert-Änderung ohne Migrationssicht auf gespeicherte Daten.

### 8. Validation (nur Benutzer)
- Das Item in der Praxis prüfen:
  - erscheint es im Inventar?
  - kann es benutzt werden?
  - hat es passende Texte?
  - kann es als Loot erscheinen?
  - kann es gespeichert und geladen werden?

## Common pitfalls
- Keine „unsichtbaren“ Items: Klasse existiert, aber kein Sprite, kein Generator-Eintrag, kein Text.
- Keine manuell parallel gepflegten Arrays in `Generator`/`StandardRoom` ohne Reihenfolge- und Größenprüfung.
- Keine neuen Item-Statusfelder, die nicht persistent gespeichert und wiederhergestellt werden.
- Keine Umbenennung von `Bundlable`-Klassen oder gespeicherten Enums ohne Migrationspfad.
- Keine magischen Zahlen für Drops, Preise, Tier oder andere Werte ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Klasse implementiert und im richtigen Paket/Typ
- [ ] Sprite und Darstellung registriert
- [ ] Texte ergänzt
- [ ] Generator/Loot/Spawn eingebaut
- [ ] Katalog/Journal/Interaktion registriert, falls nötig
- [ ] Savegame/Bundles und Migrationsfälle geprüft
- [ ] Validierung im Spielablauf durchgeführt
