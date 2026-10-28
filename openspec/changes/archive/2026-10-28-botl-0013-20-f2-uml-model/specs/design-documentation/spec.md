# Spec Delta

## Purpose

Defines the UML documentation that must accompany the code: a PlantUML class model of the domain that stays consistent with the implementation.

## ADDED Requirements

### Requirement: PlantUML class model file
The repository SHALL contain the PlantUML source `uml/model.puml` with a class diagram of the model that PlantUML accepts as valid syntax.

#### Scenario: Diagram present and valid
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** the file exists and contains a syntactically valid class diagram

### Requirement: Diagram content
The diagram SHALL show the model classes with their relevant attributes (without methods or constructors), the ticket inheritance hierarchy, the composition between a group ticket and its individual tickets, and the associations among users, groups, tickets and attractions with their directions and multiplicities.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend the abstract `Ticket`, and `GroupTicket` is composed of one or more `IndividualTicket`

#### Scenario: Directed associations
- **WHEN** the diagram is inspected
- **THEN** it shows directed associations from `IndividualTicket` to exactly one `User`, from `GroupTicket` to zero or one `Group`, from `Group` to its member `User`s and from `Attraction` to its occupant `User`s, and no association from `User` to tickets or attractions

### Requirement: Consistency with the implementation
Every model class and each of its attributes SHALL appear in the diagram, and the diagram SHALL NOT declare model classes that do not exist in the code.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a check confirms that each class of the model package and each of its fields is present in `uml/model.puml`
