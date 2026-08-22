# Guideline: Dokumentation für neue Feature- und Inhalts-Guidelines

## Ziel
Guidelines für Feature Arten, wie Items, Lebewesen, Zauber, Wände. Sie sind hauptsächlich für KI Agenten.

## Wie
Wenn die Feauture-Art oder System bereits in einer allgemeinen Guideline behandelt wird, soll die Guideline nur die neuen spezifischen Punkte für das Feature enthalten.
Dies wird am Anfang der Guideline mit "{Feature} child of {GeneralGuideline}" klar gemacht. Z.B. "DamageWand child of Wand child of Item" oder "Food child of Item".

## Was jede Guideline enthalten sollte
Die Guidelines sollen:
- kurz und prägnant sein
- die spezifischen Anforderungen der Feature-Art enthalten
- die typischen Fehler und Stolperfallen aufführen
- eine klare Definition of Done enthalten und oder eine klare Checkliste enthalten

## Was man vermeiden sollte
- Keine langen theoretischen Erläuterungen.
- Keine Wiederholungen der allgemeinen Item-Guideline in einer Spezialguideline.
- Keine ungenauen Aussagen wie „muss sauber implementiert werden“
- Keine Redundanz vor allem bei Definition of Done und Checkliste
- Keine Aufgaben die der User erledigt wie Playtests, sondern nur die Anforderungen an die Implementierung.

## Beispielstruktur
```md
# Guideline: {Feature}

## Ziel
...
## Erinnerung an allgemeine Guideline
...
## X-spezifische Anforderungen
...
## Absolute Vermeidung
...
## Definition of Done
...
## Kurz-Checkliste
- [ ] ...
- [ ] ...
```

