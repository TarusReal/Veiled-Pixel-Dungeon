# Guideline: Der Wand

## Ziel
Wand-spezifische Anforderungen für neue Zauberstäbe/Runenstäbe. Diese Guideline ist ein `Wand child of Item` und behandelt nur Dinge, die für Wandsysteme besonders relevant sind.

## Ausführliche Checklist

### 1. Wand-Basis und Effektmodell
- Der Wand als richtige Wand-Klasse/Unterklasse implementieren und in das vorhandene `Wand`-System passen.
- Wirkung, Zielbereich, Dauer, Ladezustand und Einsatzlogik klar definieren.
- Das Verhalten auf den normalen Wand-Use-Flow abstimmen: aktivieren, Ziel auswählen, Effekt ausführen, ggf. Verbrauch/Ladung.
- Sonderfälle klar modellieren: keine gültigen Ziele, leerer Stab, unvollständige Nutzung, Zielsystem- oder Reichweitengrenzen.

### 2. Wand-Identität und Abgrenzung
- Der Wand als eindeutigen Typ mit eigener Funktion und Spielrolle definieren: offensiv, kontrollierend, unterstützend, debuffend, mobilitätsbezogen usw.
- Die Wirkung mit anderen Wänden vergleichen, damit sie nicht nur eine Kopie einer bestehenden Wand ist.
- Name, Effekt, Ladezustand und visuelle Reaktion müssen zusammenpassen.

### 3. Darstellung und UI
- Ein passendes Sprite/Icon registrieren und die Darstellung zum Wand-Status passend machen.
- Wenn Ladezustand, Effektfarbe oder Animation relevant sind, diese sauber an die UI/Visuals anbinden.
- Keine Wand ohne klare visuelle Unterscheidung zwischen aktiv, geladen und leer/unnutzbar.

### 4. Texte und Messagerie
- Name, Beschreibung und Nutzungstexte in den passenden `.properties`-Dateien ergänzen.
- Effekt- und Ladezustandstexte nur dort eintragen, wo das bestehende Wand-System sie erwartet.

### 5. Spawn, Loot und Katalog
- Der Wand nur dann im Generator/Spawn einbauen, wenn sie im Spiel zufällig vorkommen soll.
- Wenn sie in Katalog, Journal oder Inventar-Listen auftauchen soll, dort registrieren.
- Sonderregeln wie Wand-Kategorien, Rarität oder Shop-Varianten nur ergänzen, wenn sie für das Design notwendig sind.

### 6. Persistenz und Savegame
- Wand-Zustände wie Ladung, Effektstände, Modifikatoren oder Sonderenergie korrekt serialisieren.
- Bei Umbenennung oder Klassenänderung die passende Alias-/Migrationslogik ergänzen.

## Common pitfalls
- Kein Wand ohne klaren Effekt und eindeutigen Nutzungskontext.
- Kein Wand, die zwar compiliert, aber nicht im passenden Wand-Flow, in der Darstellung oder im Textsystem integriert ist.
- Kein Wand, die sich in Zielsystem, Reichweite oder Ladezustand inkonsistent zum Rest der Wandsysteme verhält.
- Kein gespeicherten Wand-Zustände ohne Migrations-/Alias-Prüfung.
- Kein "magischen" Werte für Reichweite, Ladung oder Effekt ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Der Wand passt in das vorhandene Wand-System
- [ ] Effekt, Zielbereich und Ladezustand sind klar definiert
- [ ] Darstellung und UI sind registriert
- [ ] Texte ergänzt
- [ ] Loot/Spawn/Katalog, falls nötig, eingehängt
- [ ] Persistenz und Savegame geprüft
- [ ] Wand-Verhalten im relevanten System konsistent und abgeschlossen
