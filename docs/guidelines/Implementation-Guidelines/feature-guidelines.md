# Guideline: Dokumentation für neue Feature- und Inhalts-Guidelines

## Ziel
Guidelines für Feature-Arten wie Items, Kreaturen, Zauber, Wände, Regionen oder andere Inhaltsbereiche. Sie dienen vor allem KI-Agenten, aber auch Menschen, die neue Inhalte konsistent ergänzen wollen.

## Prinzip
Die effektivste Form ist eine doppelte Checkliste:
- ausführliche Checklist für den eigentlichen Implementierungsfluss
- kurze Final-Checklist als letzte Abnahmeprüfung

Das ist meist besser als lange Textblöcke, weil es die wichtigsten Prüfungen klar und schnell lesbar macht.

## Leitregel
Wenn ein allgemeines System bereits in einer allgemeinen Guideline beschrieben ist, dann enthält die spezielle Guideline nur noch die feature-spezifischen Punkte. Keine Wiederholung der allgemeinen Regeln.

## Struktur einer guten Guideline
Eine gute Feature-Guideline enthält in der Regel:
- Ziel
- ausführliche Checklist
- Common pitfalls
- kurze Final-Checklist

Diese Struktur ist klarer als "Requirements + Definition of Done + To-Do" mit starker Redundanz.

## Was man vermeiden sollte
- Keine langen Einleitungen oder theoretischen Erläuterungen.
- Keine ungenauen Formulierungen wie „muss sauber implementiert werden“ ohne überprüfbaren Punkt.
- Keine Wiederholungen zwischen ausführlicher Checklist und Final-Checklist.
- Keine Playtest-Anforderungen als Implementierungsregel (oder zumindest als nur für den Benutzer relevant kennzeichnen).
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

