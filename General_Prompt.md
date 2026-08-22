Bitte prüfe vor und nach der Implementierung die relevanten Projekt-Guidelines für neue Inhalte.

Kontext:
- Projekt: Veiled Pixel Dungeon
-Guidelines: docs/guidelines/

Aufgaben:
1. Analysiere die gewünschte Änderung.
2. Liste alle betroffenen Dateien und Systeme auf:
    - Klasse / Logik
    - Sprite / Darstellung
    - Text/Properties
    - Loot / Spawn / Registry
    - Bestiary / Journal / Catalog
    - Savegame / Bundle / Alias / Migration
3. Frage den Benutzer nach weiteren Details, falls es Unklarheiten gibt.
4. Implementiere die Änderung sauber und vollständig.
5. Prüfe explizit:
    - Ist die Registrierung nicht vergessen?
    - Gibt es Texte in den .properties-Dateien?
    - Wurde Savegame-Kompatibilität geprüft?
    - Wenn eine Klasse oder Enum umbenannt wurde: wurde ein Alias/Migration ergänzt?
6. Verfasse am Ende eine kurze Zusammenfassung mit:
    - geänderten Dateien
    - fehlenden Registrierungen geprüft
    - Validierung/Tests durchgeführt
    - offene Risiken
7. Wenn etwas bewusst nicht umgesetzt wurde, dokumentiere es explizit.

Wichtig:
- Keine „Halb-Implementierung“.
- Keine Änderungen an gespeicherten Strukturen ohne Migrationsüberlegung.
- Keine sichtbaren, aber nicht registrierten Inhalte.