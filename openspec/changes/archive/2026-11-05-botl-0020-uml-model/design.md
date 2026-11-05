# Design

## Context

Model package: `User`, `UserField` (enum), `UserValidator`, `Group`, `Ticket` (abstract), `IndividualTicket`, `GroupTicket`, `GroupDiscount` (enum), `Attraction`; central manager `park.Park` (singleton) owning the repositories.

## Goals / Non-Goals

**Goals:** a diagram an evaluator can check against the code. **Non-Goals:** documenting services/persistence internals in detail (shown only as the repositories Park manages).

## Decisions

- **Scope**: model package + `Park` + the four repositories (they are how Park "centralises access"); storage interfaces shown with their file implementations to document the persistence separation via interface implementation arrows. Commands/services omitted (not model structure).
- **Relationships**: `GroupTicket *-- "1..*" IndividualTicket : composition` (life cycle bound to the group ticket); `IndividualTicket --> "1" User`; `GroupTicket --> "0..1" Group`; `Group --> "0..*" User : members`; `Attraction --> "0..*" User : occupants`; repositories `o--` their elements; `Park --> "1"` each repository.
- **Consistency test** (`UmlModelTest`): reads the file, asserts every model class name is declared (`class|abstract class|enum|interface <Name>`) and the key relationship lines exist and no line starts with `User -->`. Syntax was checked with a locally cached PlantUML jar (`-checkonly`), not added as a build dependency (external libraries not allowed).

## Risks / Trade-offs

- [Diagram drifts in later deliveries] → the test fails when a model class is added without updating the diagram.
