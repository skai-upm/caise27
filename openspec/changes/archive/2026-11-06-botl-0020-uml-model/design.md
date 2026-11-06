# Design

## Context

See proposal.md – Why. Model classes live in `es.upm.park.model`: `User`, `UserField` (enum), `Group` (member DNIs), abstract `Ticket` (id, date, basePrice, discount), `IndividualTicket` (user), `GroupTicket` (group nullable, groupId, composition), `GroupDiscount`, `Attraction` (occupants). `ParkController` (singleton) owns the services/repositories.

## Goals / Non-Goals

**Goals:**
- A diagram faithful to the code, verified automatically.

**Non-Goals:**
- Diagramming persistence/command infrastructure in detail (shown only as the controller's owned elements where it helps).

## Decisions

- **Scope of the diagram**: model package plus `ParkController` with its single-instance self-association and its ownership of users, groups, tickets and attractions (via repositories, drawn as associations to the model elements they hold, labelled with the repository). Services and persistence stores are omitted as non-model infrastructure.
- **Group → User via DNI**: the code stores member DNIs; the diagram shows a directed association `Group --> "0..*" User : members` noted as "by DNI". The consistency test accepts an association whose field is a collection of `String` named `memberDnis` for this case (explicit mapping table in the test).
- **Consistency test (`UmlModelTest`)**: parses lines of the form `class X`, `abstract class X`, `enum X`, `X <|-- Y`, `X *-- "m" Y : label`, `X --> "m" Y : label`; checks via reflection that each class exists in `es.upm.park.model` or `es.upm.park.controller`, abstractness/enum kind match, superclass matches, and each association label names a field of X whose type (or generic element type) is Y, with the DNI exception above. No PlantUML library is added to the project; syntax is checked during verification with a locally cached PlantUML jar (`-checkonly`).
