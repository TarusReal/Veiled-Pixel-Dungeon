# Guideline: Die Rüstung

## Ziel
Rüstungs-spezifische Anforderungen für neue Schutz- oder Ausrüstungsgegenstände. Diese Guideline ist ein `Armor child of Item` und behandelt nur Dinge, die für Rüstungsysteme besonders relevant sind.

## Ausführliche Checklist

### 1. Rüstungsbasis und Effektmodell
- Die Rüstung als passende Rüstungs-Klasse/Unterklasse implementieren und in das bestehende Rüstungs-System einpassen.
- Schutzwerte, Resistenz, Gewicht, Slot-Belegung und Spezialeffekte klar definieren.
- Typische Rüstungsmerkmale sauber modellieren: Schutz gegen physische oder elementare Treffer, Mobilitäts-/Tempo-Effekte, Statusresistenzen, Spezialboni.
- Sonderfälle festlegen: Wechseln, Ausrüsten, Schaden durch Verschleiß, Verstecken in anderen Slots oder Statusverlierer.

### 2. Rüstungsidentität und Abgrenzung
- Die Rüstung als klaren Stil definieren: leicht, schwer, magisch, defensiv, anpassungsfähig, mobilitätsorientiert etc.
- Die Wirkung mit bestehender Rüstung vergleichen, damit sie nicht nur eine Kopie eines vorhandenen Typs ist.
- Name, Schutzwert, Bonus und Stil müssen zusammenpassen.

### 3. Darstellung und UI
- Ein passendes Sprite/Icon registrieren und die Darstellung auf den Rüstungsstatus abstimmen.
- Wenn Sondervisuals, Farbvarianten, EFX oder Ausrüstungsanzeige relevant sind, diese sauber anbinden.
- Keine Rüstung ohne klare visuelle Unterscheidung zwischen Ausrüstung, Verwendung und Sonderzustand.

### 4. Texte und Messagerie
- Name, Beschreibung und Nutzungstexte in den passenden `.properties`-Dateien ergänzen.
- Effekt- oder Status-Texte nur dort eintragen, wo das Rüstungs-System sie erwartet.

### 5. Spawn, Loot und Katalog
- Die Rüstung nur dann in Generator/Spawn einbauen, wenn sie im Spiel zufällig vorkommen soll.
- Wenn sie in Katalog, Journal oder Inventar-Listen auftauchen soll, dort registrieren.
- Sonderregeln wie Rüstungs-Typ, Seltenheit, Shop-Variante oder Set-Komponenten nur ergänzen, wenn sie für das Design notwendig sind.

### 6. Persistenz und Savegame
- Rüstungs-Zustände wie Schutzmodifikatoren, Boni, Zustand oder Spezialstatus korrekt serialisieren.
- Bei Umbenennung oder Klassenänderung die passende Alias-/Migrationslogik ergänzen.

## Common pitfalls
- Keine Rüstung ohne klaren Schutzkontext und eindeutige Rolle im Spieler-Loadout.
- Keine Rüstung, die zwar compiliert, aber nicht im passenden Rüstungs-Flow, in der Darstellung oder im Textsystem integriert ist.
- Keine Rüstung, die in Schutz, Gewicht oder Effekt mit dem bestehenden Rüstungs-Design inkonsistent ist.
- Keine gespeicherten Rüstungs-Zustände ohne Migrations-/Alias-Prüfung.
- Keine "magischen" Werte für Schutz, Resistenz oder Boni ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Die Rüstung passt in das vorhandene Rüstungs-System
- [ ] Schutz, Resistenz und Sondereffekte sind klar definiert
- [ ] Darstellung und UI sind registriert
- [ ] Texte ergänzt
- [ ] Loot/Spawn/Katalog, falls nötig, eingehängt
- [ ] Persistenz und Savegame geprüft
- [ ] Rüstungs-Verhalten im relevanten System konsistent und abgeschlossen
