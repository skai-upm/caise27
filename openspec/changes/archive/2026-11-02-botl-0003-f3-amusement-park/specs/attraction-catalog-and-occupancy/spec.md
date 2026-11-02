## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity, and an initially empty collection of users inside. Duplicate identifiers and non-positive or non-numeric capacities SHALL be rejected. The attraction SHALL reference its occupants; users SHALL NOT reference attractions. From Delivery 3, `ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL also be accepted with type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; the 3-parameter form SHALL create a GENERAL attraction, and an unknown type SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is reported and no attraction is created

#### Scenario: Duplicate identifier
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed and A01 exists
- **THEN** an error is reported and A01 is unchanged

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 is a SPECIAL attraction with capacity 15

#### Scenario: Legacy syntax means GENERAL
- **WHEN** `ATTRACTION CREATE;A05;Noria;8` is processed
- **THEN** A05 is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A06;X;5;VIP` is processed
- **THEN** an error is reported and no attraction is created
