# Design

## Context

`uml/model.puml` exists from earlier changes but includes methods, `{readOnly}`/`{static}` labels, dependency arrows everywhere, and some associations without navigability crosses. The code (22 production types in `es.upm.park`) is the authority. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- A diagram that can be checked line by line against the fields of each class.

**Non-Goals:**
- Any change to Java code or tests; documenting tests, private static constants or JDK classes.

## Decisions

- **Methods vs. "no methods" (explicit conflict resolution)**: the source's descriptive list mentions "relevant public methods", while its acceptance criteria require "Do not represent usage relationships or methods in classes". The acceptance criteria are the verifiable, more specific instruction, so methods are omitted. Recorded here rather than silently dropped.
- **Usage arrows**: the same criteria forbid usage relationships but require that "if a class has no incoming relationships, its usage must be shown". Combined rule: dependency arrows only for classes with no incoming association/aggregation/composition/generalization — `App` (shown using `CommandRunner`, which it creates; App itself is the entry point), `UserValidator`, `UserField`, `Result`, `TicketFileStore.Contents`, `GroupTicketFileStore.Contents`. Each gets arrows from the classes that call/create it.
- **Attributes**: instance fields of primitive/value types (`String`, `int`, `boolean`, `LocalDate`, `BigDecimal`, `Path`, `List<String>`, `Set<String>`) and enums only; visibility kept (UML standard); "technical labels" interpreted as PlantUML/UML property tags (`{readOnly}`, `{static}`, `{abstract}` on members) and stereotypes on attributes, which are removed. Private static constants are not "relevant attributes" and are omitted. Record components of `Result` and the `Contents` records are shown as their attributes; record components referencing modelled classes (`Contents.tickets`, `Contents.groupTickets`) are relationships.
- **Relationship notation (PlantUML)**: plain association `Holder "m" x--> "n" Target : attribute`; aggregation `Holder o--> Target`; composition `Holder *--> Target`; generalization `Parent <|-- Child`. The cross/diamond sits at the holder end, the arrowhead at the referenced end. Composition is used where the holder creates and exclusively owns the parts' lifecycle (`UserManager.users`, `GroupManager.groups`, `TicketManager.tickets`/`groupTickets`, `AttractionManager.attractions`, `GroupTicket.tickets`); aggregation where the holder references objects owned elsewhere (`Group.members`, `GroupTicket.group`, `Attraction.present`); association for the rest (managers → stores, managers → `UserManager`, `IndividualTicket.user`, controller → managers, runner → controller).
- **Multiplicities**: single reference `1`; `List<...>` `0..*`, except `GroupTicket.tickets` `1..*` (purchase requires members). Holder-side multiplicity is written only where the code determines it (e.g. a user can be referenced by `0..*` individual tickets).
- **Validation**: PlantUML 1.2024.7 `-checkonly`, plus a scripted cross-check that every class and relationship label in the diagram matches a field in the sources.

## Risks / Trade-offs

- [Interpretation of "technical labels"] → removed property tags/stereotypes on attributes but kept visibility markers and types, which are standard UML and needed to read the model.
