# Guideline: Die Waffe

## Ziel
Waffen-spezifische Anforderungen für neue Nahkampf- oder Fernkampf-Gegenstände. Diese Guideline ist ein `Weapon child of Item` und behandelt nur Dinge, die für Waffen besonders relevant sind.

## Ausführliche Checklist

### 1. Waffenbasis und Effektmodell
- Die Waffe als passende Waffenkategorie und Basisklasse implementieren und in das bestehende Waffen-System einpassen.
- Schaden, Reichweite, Tempo, Genauigkeit, Spezialeffekte und Einsatzlogik klar definieren.
- Typische Waffenmerkmale sauber modellieren: Trefferchance, Krits, Durchschlag, Feuer-/Störungslogik, Mehrfachangriff oder Spezialangriffe.
- Sonderfälle festlegen: leere Hand, unzureichende Reichweite, falsches Ziel, Kampf- oder Zustandsgrenzen.

### 2. Waffenidentität und Abgrenzung
- Die Waffe als klaren Stil definieren: schneidend, explosiv, präzise, schwer, magisch, treffsicher, kontrollierend etc.
- Die Wirkung mit bestehenden Waffen vergleichen, damit sie nicht nur eine Kopie eines vorhandenen Typs ist.
- Name, Effekt, Visual und Bedienung müssen zusammenpassen.

### 3. Darstellung und UI
- Ein passendes Sprite/Icon registrieren und die Darstellung auf den Waffen-Status abstimmen.
- Wenn besondere FX, Animationszustände, Waffenklingen-Variante oder Rüstungs-/Invenar-Visuals relevant sind, diese sauber anbinden.
- Keine Waffe ohne klare Unterscheidung zwischen Grundzustand, aktivem Einsatz und Spezialstatus.

### 4. Texte und Messagerie
- Name, Beschreibung und Nutzungstexte in den passenden `.properties`-Dateien ergänzen.
- Effekt- oder Status-Texte nur dort eintragen, wo das Waffen-System sie erwartet.

### 5. Loot, Spawn und Katalog
- Die Waffe nur dann in Generator/Spawn einbauen, wenn sie im Spiel zufällig vorkommen soll.
- Wenn sie in Katalog, Journal oder Inventar-Listen auftauchen soll, dort registrieren.
- Sonderregeln wie Waffenkategorie, Verteilung, Seltenheit oder Shop-Variante nur ergänzen, wenn sie für das Design notwendig sind.

### 6. Persistenz und Savegame
- Waffen-Zustände wie Aufladung, Modifikatoren, Spezialstatus oder besondere Effekte korrekt serialisieren.
- Bei Umbenennung oder Klassenänderung die passende Alias-/Migrationslogik ergänzen.

## Common pitfalls
- Keine Waffe ohne klaren Effekt und eindeutigen Kampfkontext.
- Keine Waffe, die zwar compiliert, aber nicht im passenden Waffen-Flow, in der Darstellung oder im Textsystem integriert ist.
- Keine Waffe, die in Reichweite, Tempo oder Effekt mit dem bestehenden Waffen-Design inkonsistent ist.
- Keine gespeicherten Waffen-Zustände ohne Migrations-/Alias-Prüfung.
- Keine "magischen" Werte für Schaden, Reichweite oder Spezialeffekte ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Die Waffe passt in das vorhandene Waffen-System
- [ ] Schaden, Reichweite und Spezialwirkung sind klar definiert
- [ ] Darstellung und UI sind registriert
- [ ] Texte ergänzt
- [ ] Loot/Spawn/Katalog, falls nötig, eingehängt
- [ ] Persistenz und Savegame geprüft
- [ ] Waffen-Verhalten im relevanten System konsistent und abgeschlossen
