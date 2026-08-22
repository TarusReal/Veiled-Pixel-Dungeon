# Guideline: Wand child of Item


## Basiskriterien aus der allgemeinen Item-Guideline
Bitte die allgemeine Item-Guideline immer mitdenken und erfüllen:
- Klasse korrekt anlegen
- Sprite/Darstellung registrieren
- Texte ergänzen
- Loot/Generator registrieren, falls erforderlich
- Journal/Katalog registrieren, falls nötig
- Savegame/Bundles prüfen
- Validierung durchführen

## Wand-spezifische Anforderungen

### 1. Klassen- und Systembasis
- Die Wand als passende Item-Unterklasse implementieren, z. B. als Wandspezialisierung im bestehenden Wand-System.
- Die korrekte Basisklasse und die vorhandenen Wand-Mechaniken verwenden, statt ein völlig eigenes Verhalten zu kreieren.
- Auf vorhandene Wand-Logik achten: Nutzung, Aufladung, Verbrauch, Targeting, Effekt, Cooldown/Charges, falls relevant.

### 2. Effekt- und Nutzungslogik
- Der Effekt muss sauber im Wand-Use-Flow integriert sein.
- Wenn die Wand einen Schuss, eine Rektion, einen Debuff, einen Buff oder eine Zone erzeugt, muss dieser Effekt mit den bestehenden Actor-/Targeting-Regeln kompatibel sein.
- Edge Cases prüfen: Null-Ziel, ungültige Zielsituation, fehlende Mana/Charges, Mehrfach-Activation, Respektierung von Level-/Map-Bedingungen.

### 3. Identifikation und Balance
- Die Wand muss in den bestehenden Wand-/Tier-/Power-Mechaniken passen.
- Stärken, Effektstärke, Reichweite, Kosten, Ladezyklen und Drops müssen konsistent mit der vorhandenen Balance sein.
- Keine „Wand mit Sonderregeln“, die außerhalb des Standardmodells funktionieren, ohne dass das System bewusst erweitert wurde.

### 4. Visuals und UI
- Sprite- und Icon-Registrierung korrekt ergänzen.
- Wenn die Wand im Inventar, bei der Verwendung oder im Effektvisuell anders aussieht, auch diese Darstellungen sauber integrieren.
- UI-/Tooltip-Ausgaben müssen mit den allgemeinen Item-Textregeln übereinstimmen.

### 5. Generator / Loot / Verteilung
- Wenn die Wand zufällig erscheinen soll, in die passende Wand-/Loot-Tabelle registrieren.
- Tier, Wahrscheinlichkeit und Einordnung in die übliche Wand-Verteilung respektieren.
- Neue Wandspezialitäten dürfen nicht „unreachable“ oder „zu dominant“ werden, ohne dass die Verteilung bewusst angepasst wurde.

### 6. Savegame- und Persistenzsicherheit
- Wand-Objekte und Zustandseigenschaften müssen beim Speichern/Laden konsistent bleiben.
- Spezialzustände wie Aufladungsstatus, Effektparameter, Charges, Cooldowns oder geladene Werte müssen persistierbar sein.
- Bei Änderungen an Wandspezifikationen oder gespeicherten Attributen: Migrations-/Alias-Überlegung mit einbeziehen.


## Absolute Vermeidung
- Keine Wand, die nur als Klasse existiert, aber nicht im Generator/Loot/UI/Spielfluss realisiert ist.
- Keine Sonderlogik, die das allgemeine Wand-System bricht.
- Keine Persistenzänderungen ohne Savegame-Check.
- Keine neuen Tiers/Power-Stufen ohne konsistente Einordnung.

## Definition of Done
Eine neue Wand gilt als fertig, wenn alle allgemeinen Item-Kriterien und zusätzlich diese Wand-spezifischen Punkte erfüllt sind:
- Klasse korrekt implementiert
- Effekt logisch integriert
- Darstellung registriert
- Generator/Loot passend eingebaut
- Savegame/Persistenz geprüft
- Balance und Spielfluss validiert

## Kurz-Checkliste
- [ ] Allgemeine Item-Guideline erfüllt
- [ ] Wand-Klasse korrekt eingebaut
- [ ] Effekt- und Targeting-Logik geprüft
- [ ] Sprite/UI registriert
- [ ] Loot/Generator passend
- [ ] Savegame/Persistenz geprüft
- [ ] Funktionstest durchgeführt
