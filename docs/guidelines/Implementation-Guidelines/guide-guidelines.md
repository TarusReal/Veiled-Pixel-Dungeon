# Guideline: Dokumentation für neue Feature- und Inhalts-Guidelines

## Ziel
Guidelines für Feature-Arten wie Items, Kreaturen, Zauber, Wände, Regionen oder andere Inhaltsbereiche. Sie dienen vor allem KI-Agenten, aber auch Menschen, die neue Inhalte konsistent ergänzen wollen.

## Prinzip
Die effektivste Form ist eine doppelte Checkliste:
- ausführliche Checklist für den eigentlichen Implementierungsfluss
- kurze Final-Checklist als letzte Abnahmeprüfung

## Wie
Wenn die Feauture-Art oder System bereits in einer allgemeinen Guideline behandelt wird, soll die Guideline nur die neuen spezifischen Punkte für das Feature enthalten.
Dies wird am Anfang der Guideline mit "{Feature} child of {GeneralGuideline}" klar gemacht. Z.B. "DamageWand child of Wand child of Item" oder "Food child of Item".
das child ist dann eine spezielle Guideline und enthält nur die feature-spezifischen Punkte. Keine Wiederholung der allgemeinen Regeln.

## Was man vermeiden sollte
- Keine langen Einleitungen oder theoretischen Erläuterungen.
- Keine ungenauen Formulierungen wie „muss sauber implementiert werden“ ohne überprüfbaren Punkt.
- Keine Wiederholungen zwischen ausführlicher Checklist und Final-Checklist.
- Keine Playtest-Anforderungen als Implementierungsregel (oder zumindest als nur für den Benutzer relevant kennzeichnen).
- Keine Validierungstest und keine Redundanz
- Keine Generic-Regeln in Spezial-Guidelines, wenn nur das Feature relevant ist.

## Beispielstruktur
```md
# Guideline: {Feature}

## Ziel
...

## Ausführliche Checklist
### 1. ...
- ...

### 2. ...
- ...

## Common pitfalls
- ...

## Kurze Final-Checklist
- [ ] ...
- [ ] ...
```

## Verfügbare Feature-Guidelines
- [Item-Guideline](./item-guideline.md)
- [Creature-Guideline](./creature-guideline.md)
- [Wand-Guideline](./wand-guideline.md)
- [Weapon-Guideline](./weapon-guideline.md)
- [Armor-Guideline](./armor-guideline.md)
- [Potion-Guideline](./potion-guideline.md)
- [Scroll-Guideline](./scroll-guideline.md)

