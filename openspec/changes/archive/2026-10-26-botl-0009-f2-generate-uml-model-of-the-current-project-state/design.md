# Design

## Context

The current `uml/model.puml` was extended in each change. Source analysis (fields of every class in `es.upm.parque`) gives the structural references: `App.processor`; `CommandProcessor.{userManager, groupManager, ticketManager, attractionManager, mainController}`; managers → repositories, other managers, `MainController` and their object lists; `Group.members`; `IndividualTicket.user`; `GroupTicket.{group, tickets}`; `Attraction.visitors`. `Ticket` is abstract with two subclasses; `ParkException extends RuntimeException`. `UserField`, `UserValidator`, `TextFiles` and `ParkException` are not referenced by any field.

## Goals / Non-Goals

**Goals:**
- A diagram that can be checked line by line against the code.

**Non-Goals:**
- Any code change, refactoring, or modelling of intended-but-unimplemented design.
- Nested record `TicketRepository.TicketData` (an internal transfer type) and private static constants (formatter, separators, syntax strings) are left out as non-relevant implementation detail; leaving something out never invents it.

## Decisions

- **Attributes**: primitives (`int`, `boolean`) and value types (`String`, `LocalDate`, `BigDecimal`, `Path`) only; no `{readOnly}`/`{static}` labels, no stereotypes; static configuration constants omitted.
- **Relationships**: `holder "m" x--> "n" Target : attribute` for plain associations; `GroupTicket o--> Group : group` (aggregation) and `GroupTicket *--> IndividualTicket : tickets` (composition), diamond on the holder. Multiplicities: target ends from the field type (`1` for single references, `0..*` for lists); source ends only where the domain rules fix them (e.g. an internal ticket belongs to exactly one group ticket; a user may be in many groups).
- **Classes without incoming relationships**: `UserValidator` (used by the managers), `TextFiles` (used by the repositories), `UserField` (used by `UserManager`) and `ParkException` (used by `CommandProcessor`, which reports it) are connected with `..>` usage arrows. `App` is the entry point and is shown with its outgoing association.
- **Layout**: `left to right`-free default layout; validated with the PlantUML 1.2024.7 jar (`-checkonly`) and rendered with the Smetana layout to inspect it visually.

## Risks / Trade-offs

- [Source-end multiplicities are partly judgement] → Only drawn where a domain rule makes them certain; otherwise left blank rather than guessed.
