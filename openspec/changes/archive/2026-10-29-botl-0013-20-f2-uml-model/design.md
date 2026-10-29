# Design

## Context

Production classes live in `es.upm.amusementpark`: domain (`User`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`), central management (`ParkController`, singleton), persistence (`TextFileRepository` and four repositories, `ParkStorage`), interface (`CommandProcessor`, `App`).

## Goals / Non-Goals

**Goals:** a faithful diagram and a guard keeping it in sync. **Non-Goals:** rendering images, sequence diagrams.

## Decisions

- **Relationships as implemented**: `Ticket` abstract with `IndividualTicket`/`GroupTicket` subclasses; `GroupTicket *--> "1..*" IndividualTicket : composition` (composition: owned, not shared, lifecycle tied to the group ticket); `GroupTicket --> "0..1" Group` (nullable after group deletion); `IndividualTicket --> "1" User`; `Group o--> "0..*" User : members`; `Attraction --> "0..*" User : occupants`; `ParkController` aggregates users, groups, tickets, attractions with `0..*`; no reverse references (users know nothing). Persistence: repositories `extends TextFileRepository`, `ParkStorage` composes the four; `CommandProcessor --> ParkController`.
- **Packages** in the diagram group domain / management / persistence / interface visually; the Java package is single for now.
- **Consistency test** (`UmlModelTest`): lists `src/main/java/**/*.java` file names and asserts each appears as `class <Name>` in the diagram, plus the key relationship lines. Surefire runs from the project base directory, so relative paths resolve.

## Risks / Trade-offs

- [Textual check cannot prove semantic correctness] → it catches missing classes and the most important relationships; the rest is reviewed by hand against the fields.
