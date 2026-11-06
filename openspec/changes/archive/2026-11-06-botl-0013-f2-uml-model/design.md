# Design

## Context

The design spans `model` (Group, Ticket, IndividualTicket, GroupTicket, Attraction), `controller` (the ParkController singleton, the UserDirectory, ParkStorage and ParkState ports, ParkException), `persistence` (TextParkStorage and the repositories) and `command` (the processor and runner). Users are identified by email. There is no User class yet, because the user model milestone comes later in this batch.

## Goals / Non-Goals

**Goals:** a diagram faithful to the code, readable by package, and protected against drift by a test.
**Non-Goals:** documenting methods. Generating the diagram automatically.

## Decisions

- **Packages as PlantUML `package` blocks**, model first. Relevant attributes only, with their types and visibility.
- **Relationships**:
  - `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`.
  - `GroupTicket *-- "1..*" IndividualTicket : composition` (directed from group ticket).
  - `GroupTicket --> "0..1" Group : purchaser`.
  - `ParkController o-- "*" Group/Ticket/Attraction`, `ParkController --> "1" UserDirectory/ParkStorage`.
  - `TextParkStorage ..|> ParkStorage`, with composition of its three repositories.
- **Users by email**: shown as `memberEmails`, `ownerEmail` and `occupantEmails` attributes plus a note stating that users hold no references to groups, tickets or attractions. No invented User class.
- **Consistency test**: `UmlModelConsistencyTest` lists the `.java` files of `src/main/java/es/upm/park/{model,controller,persistence}` and asserts that each type name is declared in the diagram (`class|interface|enum|record|abstract class <Name>`). Maven runs tests from the project base directory, so relative paths work.
- **Syntax check** with a locally cached PlantUML jar (`-checkonly`), run outside the build. PlantUML is not added as a project dependency (standard library only).

## Risks / Trade-offs

- [The test checks presence, not every relationship] → Relationships are reviewed manually against the code in the tasks. Presence checks catch the most common drift.
