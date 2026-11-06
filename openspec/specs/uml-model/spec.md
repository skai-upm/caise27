# uml-model Specification

## Purpose

Documents the Amusement Park design as a PlantUML class diagram kept in the repository and consistent with the implemented code.

## Requirements

### Requirement: PlantUML class model in the repository
The repository SHALL contain the PlantUML source `uml/model.puml` with a class diagram of the park model showing classes, relevant enumerations, inheritance, associations, compositions, relationship directions, multiplicities and relevant attributes, without methods or constructors.

#### Scenario: Diagram source present and well-formed
- **WHEN** `uml/model.puml` is inspected
- **THEN** it is a PlantUML document delimited by `@startuml` and `@enduml` that PlantUML parses without syntax errors

### Requirement: Required relationships represented
The diagram SHALL represent the ticket hierarchy (an abstract ticket specialized by individual and group tickets), the composition between a group ticket and its individual tickets, and the directed relationships: individual ticket → user, group ticket → group (optional), group → member users, attraction → occupant users; users MUST NOT be shown referencing tickets, groups or attractions, and groups MUST NOT be shown referencing tickets.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is read
- **THEN** `IndividualTicket` and `GroupTicket` extend abstract `Ticket`, and `GroupTicket` is composed of one or more `IndividualTicket`

#### Scenario: Directions and multiplicities
- **WHEN** the diagram is read
- **THEN** each `IndividualTicket` references exactly one `User`, each `GroupTicket` references zero or one `Group`, each `Group` references zero or more `User`, and each `Attraction` references zero or more occupant `User`, all directed away from `User`

### Requirement: Diagram consistent with the code
Every class, enumeration and inheritance relationship in the diagram SHALL exist in the code with the same name, kind and supertype, and every association drawn MUST correspond to a field of the source class whose type matches the target directly or as a collection element.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a test parses `uml/model.puml` and verifies its classes, enumerations, abstract markers, inheritance and associations against the compiled classes, failing on any mismatch

### Requirement: Delivery 3 design represented
The diagram SHALL also show the ticket access interfaces and their implementations, the generic attraction hierarchy and attraction types, the passes (abstract pass, SpeedPass, Special Pass) with their association to the individual ticket and the cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket), the invoice Builder abstraction with its concrete builder, product parts and director, the relationships created by dependency injection (repositories into controllers, password hasher into the user controller, help formatter into the help generator), and the controllers whose uniqueness is part of the design marked as singletons.

#### Scenario: Delivery 3 elements present and consistent
- **WHEN** the diagram is checked by the consistency test
- **THEN** `Pass`, `SpeedPass`, `SpecialPass`, `InvoiceBuilder`, `TextInvoiceBuilder`, `Invoice`, `InvoiceService`, `PasswordHasher`, `BCryptPasswordHasher`, `HelpFormatter`, `HelpGenerator` and `PassService` are declared with the same kind as in the code, and their inheritance, realizations and associations match the code
