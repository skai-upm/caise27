# design-model Specification

## Purpose


Defines the PlantUML class model of the application kept in `uml/model.puml`, which documents the implemented classes, their relationships and multiplicities and must stay consistent with the code.

## Requirements

### Requirement: PlantUML class model
The project SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents the implemented domain classes, enumerations and main application classes, with their associations, navigability and multiplicities, and SHALL be updated whenever classes or relationships change.

#### Scenario: Model file is valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Groups represented
- **WHEN** the model is inspected after group management is implemented
- **THEN** it shows `Group` with its association to `User` (a group has zero or more members) and the classes that manage and persist groups

### Requirement: Model faithful to the implementation
The class model SHALL be derived from the current code: every class, interface, enumeration, attribute and relationship shown SHALL exist in the code, relationships SHALL reflect the navigability actually implemented (no inverse relationships added for completeness), inheritance and implementation relationships SHALL match the code (including the ticket hierarchy), and documenting the model SHALL NOT change application behaviour.

#### Scenario: Ticket hierarchy
- **WHEN** the model is inspected
- **THEN** it shows the abstract `Ticket` specialised by `IndividualTicket` and `GroupTicket` exactly as implemented

#### Scenario: Real navigability only
- **WHEN** a class holds a reference to another class that does not hold one back (for example `IndividualTicket` → `User`)
- **THEN** the model shows a single directed relationship from the holder, with no inverse relationship

#### Scenario: No invented elements
- **WHEN** each element of the model is looked up in the source code
- **THEN** a matching class, enumeration, field or reference exists

### Requirement: Attribute conventions
Classes SHALL show only attributes whose type is a primitive, a basic value type (such as text, dates or amounts) or an enumeration. Attributes that reference other model classes SHALL be shown only as relationships. Attributes SHALL NOT carry technical labels (such as `{readOnly}` or `{static}`), and methods SHALL NOT be shown.

#### Scenario: Reference shown as relationship
- **WHEN** a class holds a reference to another class (for example `Group.members`)
- **THEN** it appears as a relationship and not in the attribute list

### Requirement: Relationship conventions
Every relationship SHALL be named after the attribute that holds it in the containing class, show multiplicities where they can be inferred from the code, and mark its direction with a cross (`x`) on the end of the class that does not hold the attribute; aggregation and composition SHALL be drawn with their diamond on the holder. Usage (dependency) relationships SHALL NOT be shown, except that a class with no incoming relationship SHALL show its usage.

#### Scenario: Named directed association
- **WHEN** `Ticket`'s subclass `IndividualTicket` holds `user`
- **THEN** the model shows `IndividualTicket` → `User` labelled `user` with a cross on the `IndividualTicket` end and multiplicity `1` at `User`

#### Scenario: Class without incoming relationships
- **WHEN** a class such as `UserValidator` is not referenced by any attribute
- **THEN** the model shows the classes that use it through usage relationships
