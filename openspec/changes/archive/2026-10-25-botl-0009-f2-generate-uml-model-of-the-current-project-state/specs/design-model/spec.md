# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the application's current classes and enumerations, with their associations, directions and multiplicities, consistent with the code. It SHALL be updated whenever classes or relationships are added. The diagram SHALL be derived from the implementation, SHALL NOT include elements that do not exist in the code, and producing it SHALL NOT change project behavior.

#### Scenario: Model is valid PlantUML
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Group relationship represented
- **WHEN** the model is inspected after groups are introduced
- **THEN** it shows `Group` with an association to `User` with multiplicity `0..*` on the member side, held by the group

#### Scenario: Only implemented elements
- **WHEN** every class, enumeration and relationship of the model is compared with the source code
- **THEN** each one exists in the code, and every production class and enumeration appears in the model

#### Scenario: Ticket hierarchy
- **WHEN** the model is inspected
- **THEN** `IndividualTicket` and `GroupTicket` specialise the abstract `Ticket`, `GroupTicket` aggregates its `Group` and is composed of `IndividualTicket`s

## ADDED Requirements

### Requirement: Model notation rules
In `uml/model.puml`: classes SHALL show no methods; attributes SHALL be only fields of primitive, value (text, number, date, path) or enumeration types, written as `name : Type` without visibility marks or other technical labels; a field that references another class of the model SHALL be drawn only as a relationship, never as an attribute.

#### Scenario: Relationship fields are not attributes
- **WHEN** the `Group` class is inspected
- **THEN** it lists `id : String` and `name : String` and no `members` attribute, which appears only as a relationship

### Requirement: Relationship notation
Every association, aggregation or composition SHALL be directed from the class that holds the field to the referenced class, reflecting the navigability present in the code without adding inverse relationships; it SHALL carry multiplicities on both ends, be named exactly like the holding field, and mark the holder end (a cross for plain associations, the diamond for aggregations and compositions). Usage (dependency) lines SHALL only be drawn for classes that have no incoming relationship.

#### Scenario: Named directed association
- **WHEN** the relationship between `IndividualTicket` and `User` is inspected
- **THEN** it reads `IndividualTicket "0..*" x--> "1" User : holder`

#### Scenario: Class without incoming relationships
- **WHEN** a class such as `UserValidator` or `UserField` has no incoming association
- **THEN** a usage line from the classes that use it is shown, and no other usage lines exist
