# design-documentation Specification

## Purpose

Defines the design documentation that must accompany the code: a PlantUML class diagram of the model that stays consistent with the implementation.

## Requirements

### Requirement: PlantUML model diagram
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the model showing classes, interfaces and relevant enumerations with relevant attributes (methods and constructors omitted), inheritance, interface implementations, associations, compositions, relationship directions and multiplicities.

#### Scenario: Diagram present and valid
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it is parsed without syntax errors as a class diagram

### Requirement: Diagram consistent with the implementation
The diagram SHALL correspond to the implemented code: every class, abstract class, interface and enumeration of the model package and the central park manager appears with its actual name; the ticket hierarchy (abstract ticket specialised by individual and group tickets), the composition of a group ticket by its individual tickets, and the unidirectional associations ticket → user, group ticket → group (0..1), group → users, and attraction → occupants SHALL be represented with their directions and multiplicities; no association from user to tickets, groups or attractions SHALL appear.

#### Scenario: Model classes covered
- **WHEN** the model package classes are compared with the diagram
- **THEN** each of them is declared in the diagram

#### Scenario: Key relationships represented
- **WHEN** the diagram is inspected
- **THEN** it declares `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`, a composition from `GroupTicket` to `IndividualTicket`, a directed association from `IndividualTicket` to `User`, from `GroupTicket` to `Group` with multiplicity `0..1`, from `Group` to `User` and from `Attraction` to `User`
- **AND** no association starts at `User`

### Requirement: Delivery 3 design in the diagram
`uml/model.puml` SHALL represent the final Delivery 3 design consistently with the code, making identifiable: the ticket access interfaces and their implementation by tickets; the generic attraction type and its GENERAL/INDIVIDUAL/GROUP/SPECIAL subtypes with their bounds; passes and their relationship to individual tickets with cardinalities (0..* SpeedPasses and 0..1 Special Pass per individual ticket); the invoice Builder abstraction, concrete builders and invoice; the singleton controllers; and the dependency-injection relationships established by the composition root.

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it declares `TicketAccess`, `IndividualAccess`, `GroupAccess`, `Attraction<T extends TicketAccess>` and its four subtypes, `SpeedPass` and `SpecialPass` related to `IndividualTicket` with `0..*` and `0..1`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `Invoice`, the controllers marked as singletons, and `ApplicationInitializer` with its injection dependencies
