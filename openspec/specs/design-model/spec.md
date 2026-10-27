# design-model Specification

## Purpose

Documents the application's class design in a PlantUML diagram stored with the code and kept consistent with the implementation.

## Requirements

### Requirement: PlantUML model file
The repository SHALL contain the PlantUML source `uml/model.puml` with a class diagram of the final Delivery 2 design, showing classes, interfaces, relevant enumerations, relevant attributes, inheritance, implementations, associations, compositions, directions and multiplicities, without methods or constructors.

#### Scenario: File present and well formed
- **WHEN** `uml/model.puml` is opened
- **THEN** it starts with `@startuml`, ends with `@enduml` and declares the model classes

### Requirement: Required relationships represented
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the directed relationships among users, groups, tickets and attractions with multiplicities.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend abstract `Ticket`, and `GroupTicket` is composed of `1..*` `IndividualTicket`

#### Scenario: Directed relationships
- **WHEN** the diagram is inspected
- **THEN** it shows `IndividualTicket` → `User` (1), `GroupTicket` → `Group` (0..1), `Group` → `User` (0..*), `Attraction` → `User` (0..*), with no arrows from `User` or `Group` to tickets or attractions

### Requirement: Diagram consistent with code
Every class and enumeration of the model package SHALL appear in the diagram, and every attribute shown for a model class SHALL exist in that class.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a test compares the diagram with the model classes and fails on any missing class or non-existent attribute

### Requirement: Delivery 3 design represented
`uml/model.puml` SHALL represent the final Delivery 3 design: the access interfaces implemented by tickets, the generic attraction hierarchy and attraction types, the relationships between individual tickets and passes with their cardinalities (0..* SpeedPasses, 0..1 Special Pass per ticket), the Builder abstractions and director used to build user and group invoices, the singleton controllers and the dependencies injected at initialization.

#### Scenario: Diagram content
- **WHEN** the diagram is inspected
- **THEN** it contains the access interfaces, `Attraction<T extends AttractionAccess>` with its four subclasses, `SpeedPass`/`SpecialPass` linked to `IndividualTicket` with `0..*` and `0..1`, `InvoiceBuilder`/`StandardInvoiceBuilder`/`InvoiceController`, the `<<singleton>>` controllers and `ApplicationInitializer` injections

#### Scenario: Consistency check covers controllers and invoices
- **WHEN** the test suite runs
- **THEN** every class of the model, controller and invoice packages appears in the diagram and every attribute shown exists in the code
