Bitte prüfe vor und nach der Implementierung die relevanten Projekt-Guidelines für neue Inhalte.

Kontext:
- Projekt: Veiled Pixel Dungeon
-Guidelines: docs/guidelines/

Aufgaben:
1. Analysiere die gewünschte Änderung.
2. Frage den Benutzer nach weiteren Details, falls es Unklarheiten gibt.
3. Schreibe die Änderung als Idee gemäß docs/guidelines/Plan-Guidelines in den Ordner docs/New-Features/
4. Liste alle betroffenen Dateien und Systeme auf:
   - Klasse / Logik
   - Sprite / Darstellung
   - Text/Properties
   - Loot / Spawn / Registry
   - Bestiary / Journal / Catalog
   - Savegame / Bundle / Alias / Migration
5. Frage den Benutzer nach weiteren Details, falls es Unklarheiten gibt.
6. Implementiere die Änderung sauber und vollständig gemäß docs/guidelines/Implementation-Guidelines
7. Prüfe explizit:
    - Ist die Registrierung nicht vergessen?
    - Gibt es Texte in den .properties-Dateien?
    - Wurde Savegame-Kompatibilität geprüft?
    - Wenn eine Klasse oder Enum umbenannt wurde: wurde ein Alias/Migration ergänzt?
8. Verfasse am Ende eine kurze Zusammenfassung mit:
    - geänderten Dateien
    - fehlenden Registrierungen geprüft
    - offene Risiken
9. Wenn etwas bewusst nicht umgesetzt wurde, dokumentiere es explizit.

