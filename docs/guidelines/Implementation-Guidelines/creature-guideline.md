# Guideline: Creature

## Ziel
Eine neue Kreatur muss im Spiel vollständig funktionieren: als Gegner mit passendem Verhalten, Darstellung, Spawn-Logik und sichtbaren Spiel-Integrationen. Eine reine Klassen-Datei reicht nicht.

## Ausführliche Checklist

### 1. Klasse und Verhalten
- Die Klasse im passenden Paket unter `actors/` oder `actors/mobs/` anlegen und die passende Basis-Klasse verwenden (`Mob`, `Char`, `Hero`, `Immovable` usw.).
- Die Kampf- und Verhaltenslogik sauber definieren: HP, Reichweite, Angriff, Bewegung, Spezialaktionen, Angriffs- und Fluchtmuster.
- Wenn die Kreatur Zustände, Buffs, Debuffs oder Spezialfähigkeiten nutzt, diese in das bestehende System sauber integrieren.

### 2. Kampf- und Balance-Mechanik
- Die relevanten Stats und Schwächen/Resistenzen definieren.
- Loot- und Reward-Mechaniken prüfen, wenn die Kreatur besondere Drops, Quest-Trigger oder Boss-Logik hat.
- Keine Balance-Regel „nebenbei“ ändern; nur im Rahmen der beabsichtigten Kreatur-Implementierung anpassen.

### 3. Darstellung
- Sprite, Animationen oder `MobSprite` korrekt verknüpfen.
- Externe Assets, Farben, Varianten oder Spezialeffekte sauber registrieren.

### 4. Texte und Benennung
- Name und Beschreibung in den passenden `.properties`-Dateien ergänzen.
- Keine hardcodierten Texte im Code verwenden.
- Wenn die Benennung aus dem Klassennamen abgeleitet wird, die Namenswahl konsistent halten.

### 5. Spawn und Region-Integration
- Die Kreatur dort registrieren, wo sie auftaucht: Spawn-Tabellen, Region-/Depth-Logik, Sonder- oder Boss-Mechaniken.
- Wenn sie in Bestiary, Journal, Katalog oder Ereignislogik auftauchen soll, dort ebenfalls einbinden.

### 6. Savegame / Persistenz
- Wenn die Kreatur Zustände oder gespeicherte Daten enthält, die Persistenz prüfen.
- Bei Klassen- oder Enum-Umbenennungen die passende Alias-/Migrationslogik ergänzen.
- Keine stillen Veränderungen an gespeicherten Werten ohne Migrationsprüfung.

### 7. Validation (nur Benutzer)
- Die Kreatur in der Praxis prüfen:
  - erscheint sie im richtigen Gebiet oder bei korrekter Spawn-Bedingung?
  - zeigt sie das richtige Verhalten im Kampf?
  - hat sie Text, Darstellung und passende Drops?
  - lässt sie sich problemlos speichern/laden?

## Common pitfalls
- Keine Kreatur ohne AI, Kampf- und Spawn-Integration.
- Keine Kreatur ohne Sprite- oder Render-Anbindung.
- Keine Texte nur im Code, aber nicht in den Properties.
- Keine Spawn-/Tier-Änderungen per Hand in parallelen Arrays ohne Reihenfolge- und Größenprüfung.
- Keine Umbenennung von `Bundlable`-Klassen oder gespeicherten Enums ohne Migrationspfad.

## Kurze Final-Checklist
- [ ] Klasse korrekt im Actor-System eingebaut
- [ ] Verhalten, Stats und Kampflogik funktionieren
- [ ] Darstellung und Texte vorhanden
- [ ] Spawn/Region/Journal registriert
- [ ] Savegame-/Persistenzpunkte geprüft
- [ ] praktische Funktionsprüfung durchgeführt
