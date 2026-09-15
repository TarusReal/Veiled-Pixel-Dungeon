# Guideline: Die Schriftrolle

## Ziel
Schriftrollen-spezifische Anforderungen für neue Scrolls oder aktive Schriftstücke. Diese Guideline ist ein `Scroll child of Item` und behandelt nur Dinge, die für Schriftrollen besonders relevant sind.

## Ausführliche Checklist

### 1. Schriftrollenbasis und Effektmodell
- Die Schriftrolle als passende Scroll-Klasse/Unterklasse implementieren und in das bestehende Scroll-System einpassen.
- Wirkung, Zielsystem, Reichweite, Dauer und Verbrauchslogik klar definieren.
- Typische Scroll-Merkmale sauber modellieren: direktes Lesen, gezieltes Ziel, Selbstwirksamkeit, Status- oder Umweltwirkung.
- Sonderfälle festlegen: fehlende Zielwahl, Leerer Scroll-Slot, Nutzung im Kampf oder außerhalb, aktive Sonderregeln.

### 2. Schriftrollenidentität und Abgrenzung
- Die Schriftrolle als klaren Stil definieren: offenbart, kontrollierend, offensiv, defensiv, temporär, umgebungsorientiert etc.
- Die Wirkung mit bestehenden Scrolls vergleichen, damit sie nicht nur eine Kopie eines vorhandenen Typs ist.
- Name, Effekt, Verwendung und Spielgefühl müssen zusammenpassen.

### 3. Darstellung und UI
- Ein passendes Sprite/Icon registrieren und die Darstellung auf den Scroll-Status abstimmen.
- Wenn Farbvariante, Symbolstatus, Schriftstil oder Spezial-Animation relevant sind, diese sauber anbinden.
- Keine Schriftrolle ohne klare visuelle Unterscheidung zwischen Buch, Scroll, Lesezustand und Verbrauch.

### 4. Texte und Messagerie
- Name, Beschreibung und Nutzungstexte in den passenden `.properties`-Dateien ergänzen.
- Effekt- oder Status-Texte nur dort eintragen, wo das Scroll-System sie erwartet.

### 5. Spawn, Loot und Katalog
- Die Schriftrolle nur dann in Generator/Spawn einbauen, wenn sie im Spiel zufällig vorkommen soll.
- Wenn sie in Katalog, Journal oder Inventar-Listen auftauchen soll, dort registrieren.
- Sonderregeln wie Scroll-Typ, Seltenheit, Shop-Variante oder Rezeptbezug nur ergänzen, wenn sie für das Design notwendig sind.

### 6. Persistenz und Savegame
- Scroll-Zustände wie Lesebereitschaft, Modifikatoren oder Spezialaufladung korrekt serialisieren.
- Bei Umbenennung oder Klassenänderung die passende Alias-/Migrationslogik ergänzen.

## Common pitfalls
- Keine Schriftrolle ohne klaren Effekt und eindeutigen Nutzungskontext.
- Keine Schriftrolle, die zwar compiliert, aber nicht im passenden Scroll-Flow, in der Darstellung oder im Textsystem integriert ist.
- Keine Schriftrolle, die in Wirkung, Reichweite oder Verbrauch mit dem bestehenden Scroll-Design inkonsistent ist.
- Keine gespeicherten Scroll-Zustände ohne Migrations-/Alias-Prüfung.
- Keine "magischen" Werte für Effekt, Reichweite oder Dauer ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Die Schriftrolle passt in das vorhandene Scroll-System
- [ ] Wirkung, Zielbereich und Verbrauch sind klar definiert
- [ ] Darstellung und UI sind registriert
- [ ] Texte ergänzt
- [ ] Loot/Spawn/Katalog, falls nötig, eingehängt
- [ ] Persistenz und Savegame geprüft
- [ ] Scroll-Verhalten im relevanten System konsistent und abgeschlossen
