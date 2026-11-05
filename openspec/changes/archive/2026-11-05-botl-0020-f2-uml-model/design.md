# Design

## Context

The code has these packages:
- `model`: `User`, `UserField`, `UserValidator`, `Group`, `Ticket` (abstract), `IndividualTicket`, `GroupTicket`, `Attraction`.
- `repository`: `User`, `Group`, `Ticket` and `Attraction` repositories.
- `park`: `ParkController` and `SystemValues`.
- `persistence`: `UserFileStore`, `ParkFileStore` and `StorageLocation`.
- `command`: the processor and the per-family command classes.
- `App`.

Source §14 asks for the model structure, not every class.

## Goals / Non-Goals

**Goals:**
- A diagram that mirrors fields and reference directions exactly, with automated drift detection.

**Non-Goals:**
- Diagramming command parsing classes in detail. They are shown only as the users of `ParkController`, to keep the model readable.

## Decisions

- **Packages as PlantUML `package` blocks**, using the fully qualified class names from the code, so the consistency test can map each declared class to a source file.
- **Relationship notation**:
  - `<|--` for inheritance.
  - `*--` for the composition `GroupTicket "1" *-- "1..*" IndividualTicket : composition`.
  - Directed `-->` associations with multiplicities, following the field that holds the reference:
    - `IndividualTicket "*" --> "1" User : user`
    - `GroupTicket "*" --> "0..1" Group : group`
    - `Group "*" o--> "*" User : members` (aggregation, since users exist independently)
    - `Attraction "*" o--> "*" User : occupants`
    - `ParkController "1" *--> "1" <X>Repository`
    - `UserRepository --> UserFileStore`
    - `ParkController --> "0..1" ParkFileStore`
- **Enumeration** `UserField` is shown with its constants.
- **Utility classes** (`UserValidator`, `SystemValues`) are shown as stereotyped `<<utility>>` classes without associations. They hold no state.
- **Consistency test**: a regex-free text scan. It collects `class|enum|interface <Name>` declarations from the `.puml` file. For the four packages, it lists the `.java` file names under `src/main/java/.../{model,repository,park,persistence}`. It asserts both directions, plus a few key relationship lines.
- **Syntax check**: run `java -jar plantuml.jar -checkonly uml/model.puml` with the locally available PlantUML jar during verification. It is not part of the Maven build, so no dependency is added.

## Risks / Trade-offs

- [The test checks presence and key edges, not every multiplicity] → Multiplicities are reviewed manually against the fields during this change. The test guards against class drift.

## Review against the code

Each association was checked against the field that holds it:

| Diagram edge | Field in code | Multiplicity |
| --- | --- | --- |
| `IndividualTicket` → `User` | `IndividualTicket.user` (final, non-null) | `*` → `1` |
| `GroupTicket` → `Group` | `GroupTicket.group` (null after `GROUP DELETE`) | `*` → `0..1` |
| `GroupTicket` ◆ `IndividualTicket` | `GroupTicket.composition` (immutable, non-empty) | `0..1` → `1..*` (stand-alone individual tickets belong to no group ticket) |
| `Group` → `User` | `Group.members` | `*` → `*` |
| `Attraction` → `User` | `Attraction.occupants` | `*` → `*` |
| `ParkController` → repositories | `users`, `groups`, `tickets`, `attractions` | `1` → `1` each |
| `ParkController` → `ParkFileStore` | `store` (null for in-memory executions) | `1` → `0..1` |
| `UserRepository` → `UserFileStore` | `store` (null for the in-memory constructor) | `1` → `0..1` |

`User` declares no field referencing tickets, groups or attractions, so no edge starts at `User`. During the review, the composition's whole-side multiplicity was corrected from `1` to `0..1`.
