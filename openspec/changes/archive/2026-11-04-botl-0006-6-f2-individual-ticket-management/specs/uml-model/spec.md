# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram representing the application's current classes, including `Group` and `Ticket`, their relationships with `User`, and the classes that manage and persist them, with multiplicities.

#### Scenario: Model is valid and current
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it parses without errors and contains `Group`, `GroupManager`, `GroupFileStore` and a `Group`–`User` association with multiplicities on both ends

#### Scenario: Ticket classes represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `Ticket`, `TicketManager`, `TicketFileStore` and a `Ticket`–`User` association where each ticket refers to exactly one user and a user has zero or more tickets
