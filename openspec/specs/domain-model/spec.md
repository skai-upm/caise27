# domain-model Specification

## Purpose

Keeps a PlantUML class model in the repository that documents the application's classes, their relationships and multiplicities as the system evolves.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain a valid PlantUML class diagram at `uml/model.puml`, derived from the current implementation, that includes every application class, interface and enumeration; inheritance and realisation relationships; associations, aggregations and compositions with their navigability and multiplicities where they can be inferred from the code. It SHALL NOT include elements, relationships or inverse navigations that do not exist in the code, and SHALL be updated whenever a change adds or alters classes or relationships.

#### Scenario: Group relationship documented
- **WHEN** the model is inspected
- **THEN** it shows `Group` navigating to `User` through `members` with multiplicity `0..*`, and the managers and storage classes with the references they hold

#### Scenario: Model is valid PlantUML
- **WHEN** the file is processed by PlantUML
- **THEN** it is accepted without syntax errors, starting with `@startuml` and ending with `@enduml`

#### Scenario: Ticket hierarchy and relationships match the code
- **WHEN** the model is inspected
- **THEN** `Ticket` realises `Admission`, `IndividualTicket` and `GroupTicket` extend `Ticket`, `GroupTicket` aggregates its `group` and is composed of its `components`, `IndividualTicket` navigates to its `owner` `User`, and `Attraction` navigates to the `present` users

### Requirement: Model notation rules
The class model SHALL show no methods; SHALL show as attributes only primitive, value (such as `String`, `LocalDate`, `BigDecimal`, `Path`) and enumeration types, without technical labels such as `{readOnly}`, `{static}` or stereotypes; SHALL represent every reference to another application type as a relationship named exactly as the holding attribute, drawn with a cross at the holder's end and an arrow at the referenced type (or a diamond at the holder's end for aggregation and composition); and SHALL NOT show usage relationships except to show how a class without incoming relationships is used.

#### Scenario: No relationship attributes
- **WHEN** a class holds a reference such as `Group.members`
- **THEN** the model shows a `members` relationship from `Group` to `User` and no `members` attribute inside `Group`

#### Scenario: Class without incoming relationships
- **WHEN** a class such as `UserValidator` is not referenced by any attribute
- **THEN** the model shows a usage dependency from the classes that use it
