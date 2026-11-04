# uml-model Specification

## Purpose

Keeps a PlantUML class model in the repository that documents the application's classes, relationships and multiplicities as they are implemented.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram consistent with the implementation, representing the application's current classes, including `Group`, the ticket hierarchy, `Attraction` and their relationships with `User`, and the classes that manage and persist them, with multiplicities.

#### Scenario: Model is valid and current
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it parses without errors and contains `Group`, `GroupManager`, `GroupFileStore` and a `Group`–`User` association with multiplicities on both ends

#### Scenario: Ticket classes represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `TicketManager`, `TicketFileStore` and an individual-ticket–`User` association where each individual ticket refers to exactly one user and a user has zero or more tickets

#### Scenario: Group ticket relationships represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it shows abstract `Ticket` specialized by `IndividualTicket` and `GroupTicket`, an aggregation from `GroupTicket` to `Group` (each group ticket belongs to exactly one group), a composition from `GroupTicket` to its one or more `IndividualTicket`s, and `GroupTicketFileStore`

#### Scenario: Attractions represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `Attraction` with its association to the `User`s present (0..* per attraction), `AttractionManager`, `AttractionFileStore`, and the abstract `getHolders()` operation of `Ticket` used for uniform access
