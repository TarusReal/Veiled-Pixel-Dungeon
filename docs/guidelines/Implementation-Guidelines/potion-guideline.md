# Guideline: Der Trank

## Ziel
Trank-spezifische Anforderungen für neue Verbrauchs- oder Effekt-Items. Diese Guideline ist ein `Potion child of Item` und behandelt nur Dinge, die für Tränke besonders relevant sind.

## Ausführliche Checklist

### 1. Trank-Basis und Effektmodell
- Den Trank als passende Trank-Klasse/Unterklasse implementieren und in das bestehende Trank-System einpassen.
- Wirkung, Dauer, Zielgruppe, Verbrauchslogik und Sonderbedingungen klar definieren.
- Typische Trankmerkmale sauber modellieren: Selbst-/Zielwirkung, Tempo, Buffs, Debuffs, Heilung, Ressourcenerhalt oder Statuswechsel.
- Sonderfälle festlegen: keine gültigen Ziele, falscher Einsatzkontext, Nutzung im Kampf oder außerhalb, zeitliche Grenzen.

### 2. Trankidentität und Abgrenzung
- Den Trank als klaren Effektstil definieren: Heilung, Kampfverbesserung, Kontrolle, Verführung, Debuff, Schutz, Mobilität etc.
- Die Wirkung mit bestehenden Tränken vergleichen, damit sie nicht nur eine Kopie eines vorhandenen Typs ist.
- Name, Effekt, Konsum und Spielgefühl müssen zusammenpassen.

### 3. Darstellung und UI
- Ein passendes Sprite/Icon registrieren und die Darstellung auf den Trank-Status abstimmen.
- Wenn Farbvariante, Effektanimation, Inventar-Darstellung oder Nutzungseffekt relevant sind, diese sauber anbinden.
- Kein Trank ohne klare visuelle Unterscheidung zwischen Genuss, Wirkung und Leer-/Verbrauchszustand.

### 4. Texte und Messagerie
- Name, Beschreibung und Nutzungstexte in den passenden `.properties`-Dateien ergänzen.
- Effekt- oder Status-Texte nur dort eintragen, wo das Trank-System sie erwartet.

### 5. Spawn, Loot und Katalog
- Den Trank nur dann in Generator/Spawn einbauen, wenn er im Spiel zufällig vorkommen soll.
- Wenn er in Katalog, Journal oder Inventar-Listen auftauchen soll, dort registrieren.
- Sonderregeln wie Trank-Typ, Seltenheit, Shop-Variante oder Rezeptbezug nur ergänzen, wenn sie für das Design notwendig sind.

### 6. Persistenz und Savegame
- Trank-Zustände wie Verbrauch, Dauer, Modifikatoren oder Statuswechsel korrekt serialisieren.
- Bei Umbenennung oder Klassenänderung die passende Alias-/Migrationslogik ergänzen.

## Common pitfalls
- Kein Trank ohne klaren Effekt und eindeutigen Nutzungskontext.
- Kein Trank, der zwar compiliert, aber nicht im passenden Trank-Flow, in der Darstellung oder im Textsystem integriert ist.
- Kein Trank, der in Wirkung, Dauer oder Zielbereich mit dem bestehenden Trank-Design inkonsistent ist.
- Keine gespeicherten Trank-Zustände ohne Migrations-/Alias-Prüfung.
- Keine "magischen" Werte für Wirkung, Dauer oder Verbrauch ohne nachvollziehbare Semantik.

## Kurze Final-Checklist
- [ ] Der Trank passt in das vorhandene Trank-System
- [ ] Wirkung, Dauer und Zielbereich sind klar definiert
- [ ] Darstellung und UI sind registriert
- [ ] Texte ergänzt
- [ ] Loot/Spawn/Katalog, falls nötig, eingehängt
- [ ] Persistenz und Savegame geprüft
- [ ] Trank-Verhalten im relevanten System konsistent und abgeschlossen
