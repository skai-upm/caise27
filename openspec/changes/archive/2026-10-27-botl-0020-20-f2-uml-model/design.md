# Design

## Context

All production classes live in package `es.upm.amusementpark`. Relationships in code: `Group.members: List<User>`, `IndividualTicket.user: User`, `IndividualTicket.groupTicket: GroupTicket` (owning group ticket, nullable), `GroupTicket.group: Group` (nullable), `GroupTicket.composition: List<IndividualTicket>`, `Attraction.occupants: List<User>`, `ParkController` holding one repository of each kind plus date and price, repositories holding their elements and optional file storages.

## Goals / Non-Goals

**Goals:**
- A faithful class diagram of the Phase 2 code, checked automatically for type coverage.

**Non-Goals:**
- Sequence diagrams, rendered images (only the `.puml` source is required).

## Decisions

- **Scope:** all production types (domain, services, repositories, storages, controller, command layer, exceptions) so the "every class appears" check is meaningful; domain classes show relevant attributes; supporting classes are shown with key associations only.
- **Notation:** `abstract class Ticket`, `enum UserField`, `<|--` for inheritance, `*--` for composition (GroupTicket–IndividualTicket, `1` to `1..*`), `-->` with multiplicities for directed associations, `..>` for dependencies (services → controller, `UserField`). The back-reference `IndividualTicket --> "0..1" GroupTicket` is drawn because it exists in the code.
- **Consistency test (`UmlModelTest`):** reads `uml/model.puml` and the source directory; asserts each `*.java` type name is declared (`class|abstract class|enum|interface <Name>`) and each declared name has a source file; also checks key relationship lines.
- **Validation:** PlantUML syntax check with the PlantUML jar from the local Maven repository (`java -jar plantuml.jar -checkonly`), not a build dependency (Delivery 2 forbids external libraries in the application).
- **Mandatory commit:** `[F2-C09] Add PlantUML class model` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Diagram drifts from later code changes] → `UmlModelTest` fails when a class is added or removed without updating the diagram.
