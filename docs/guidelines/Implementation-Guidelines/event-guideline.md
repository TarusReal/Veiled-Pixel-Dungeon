# Events-Guideline

So werden neue Events richtig angebunden (generisch, mit dem Blutaltar als durchgängiges Beispiel).
Gültig für `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/events/`, `levels/rooms/special/EventRoom.java` und verwandte Systeme.

## Grundgerüst eines Events

1. **Event-Klasse**: Unterklasse von `DungeonEvent` (Paket `events`). Pflicht-Methoden:
   - `key()`: eindeutiger Bundle-Schlüssel (z. B. `bloodaltar`).
   - `canAppear()`: Gate, wann das Event im Pool auftauchen darf. Regionsabgleich über `inRegion(...)`, Event-Bedingungen kombinieren, `hasHero()` prüfen.
   - `choices(stage)`, `label(stage, i)`, `result(stage, i)`, `nextStage(stage, i)`: Dialogfluss. `nextStage = -1` beendet das Event.
   - `apply(hero, stage, choice)`: Effekt der Wahl; erzeugt State-Änderungen (Inventar, Held, `EventRunState`).
2. **Texte EN/DE**: `core/src/main/assets/messages/events/events.properties` + `events_de.properties`. Schlüssel: `events.<key>.title/text<stage>/choice<stage>_<n>/result<stage>_<n>/log`. Ton: Sprecher sprechen den Spieler direkt an; in der Blutaltar-Kammer ist das „Ihr/Euch"-Register der Priester-Sprache beibehalten. Jeder Schlüssel muss in beiden Sprachen auflösen; Tests schlagen an, wenn ein Schlüssel fehlt.
3. **Registrierung**: Klasse in `DungeonEvent.ALL` eintragen. Pool (`DungeonEvent.initForRun()/createRandom()`) vergibt jedes Event nur einmal pro Run und überlebt Save/Load über `Bundle`.
4. **Debug-Ebene 0**: `Region0Level` hält einen Geist je Event; neue Events in `ALL` bekommen dort automatisch einen Platz. Geometrie der Kammern ist in Tests abgesichert.
5. **Testen**: `test/EventSmokeTest` erweitern (Texte EN+DE, `canAppear`-Fälle, Apply-Effekt, Bundle-Restore).

## Special room & NPC

- Räume: `rooms.special.SpecialRoom` (mind. 7x7). Beim `paint()` wird das Event gezogen (`DungeonEvent.createRandom()`), nicht beim Spawnen: übersprungene Events gelten für den Run als vergeben.
- Event-Träger: `EventSpirit` (generischer Geist) oder eigene NPC-Unterklasse. Blutaltar-Muster: `BloodPriest` (`extends EventSpirit`) mit eigenem Sprite (`sprites/BloodPriestSprite.java`), das Event wird via `setEvent(event)` beim Paint gesetzt.
- Dialog: Wandmaker-Stil. `WndXXX extends WndCompactOptions` (kompakt, kleine Buttons), Ergebnisbestätigung über `WndQuest`; beim Verlassen ruft das Herausfrage-Fenster `priest.finished()`; das Verschwinden nutzt `ElmoParticle.FACTORY` wie beim Shopkeeper.
- Bestiary: NPC-Klasse in eine Bestiary-Liste aufnehmen (z. B. `Bestiary.NEUTRAL`), dazu `actors.<name>.name/desc/discover_hint` EN/DE.

## Blutaltar-spezifische Anmerkungen (Referenz)

- **Ritualkammer**: 5x5 Boden (`EventRoom.BloodRitualFloor`, Textur 80 px breit = 5 Tiles); die Altar-Grafik (16 px) steht auf `Terrain.CUSTOM_DECO` (unsichtbar + solide). `EventRoom.furnishBloodAltar(level, cx, cy, wallY)` baut Boden, Altar und Rückwand gemeinsam, damit Zufallsraum und Debug-Ebene identisches Layout bekommen.
- **Untersuchbarkeit**: `BloodRitualFloor.image()` gibt auf der Altar-Zelle `null` zurück, damit die Abfrage den Altar statt des Bodens findet; beide Klassen überschreiben `name()`/`desc()`. Schlüssel in `levels.properties` + `levels_de.properties`: `levels.rooms.special.eventroom$bloodritualfloor.*` und `...$bloodaltarvis.*`. Jedes untersuchbare Custom-Tile braucht name/desc in beiden Sprachen.
- **TP-Sperre (5%)**: permanent, nicht einmalig. Zustand ist das Feld `Hero.bloodSeal`, gesichert in `storeInBundle`/`restoreFromBundle`. Neues max HP = theoretische Max minus Sperre. Sperre = 5% der theoretischen Max, aufgerundet (21 TP → Sperre 2 → 19 verfügbar). Umsetzung: `theoreticalHT()` (unverändert inkl. Ring- und Elixir-Buffs), `sealedHT()`, `updateHT()`. UI: `StatusPane` zeichnet den gesperrten Anteil als schwarz-lila Segment (halbtransparent `0x994A0B6B`, immer 1 px Überlappung nach links, damit Rundung keine dunkle Lücke lässt); die HP-Anzeige in der Leiste bleibt `HP/Max`. Das Stats-Fenster (`WndHero`) zeigt `[theoretische max TP] -[Reduktion in absoluten Zahlen]`.
- **Gate**: `canAppear()` verlangt `theoreticalHT() >= 25` und `bloodSeal == false`. Da das Event einmal pro Run vergeben wird, ist ein zweiter Pakt ausgeschlossen; neue Gates müssen denselben Schutz prüfen.

## Checkliste für ein neues Event

- [ ] Eindeutiger `key()`, in `DungeonEvent.ALL` registriert
- [ ] `canAppear()` sinnvoll belegt (Region + Event-Bedingung)
- [ ] Sämtliche Texte lösen in EN und DE auf (auch `log`, auch alle Stages)
- [ ] Event-Dialog im Wandmaker-Stil (kompakt), Ergebnis via `WndQuest`
- [ ] NPC-/Sprite-/Bestiary-Pakete vollständig (falls NPC)
- [ ] Event-Raum in Debug-Ebene 0 einbauen
- [ ] Apply-Effekt ist save/load-fest (Bundle-Keys mit Schlüsselkonstanten, kein verlorenes Zustandsfeld)
- [ ] `EventSmokeTest` erweitert und grün
- [ ] Build: `./gradlew :core:test -Dorg.gradle.java.home=/home/atrebas/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2`
- [ ] `CLAUDE.md` und `Agent.md` bleiben unberührt
