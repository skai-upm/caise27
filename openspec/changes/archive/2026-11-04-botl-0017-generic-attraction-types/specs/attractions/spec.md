# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
The system SHALL process `ATTRACTION CREATE;Id;Nombre;Capacidad` and `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`, creating the attraction only if the identifier and name are non-blank, the identifier is not used by another attraction, the capacity is a positive integer written with digits only, and the optional type is one of `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL` (case-insensitive). Without a type the attraction SHALL be `GENERAL`. Otherwise it SHALL print a line starting with `ERROR` and create nothing.

#### Scenario: Create
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed and no attraction `A01` exists
- **THEN** the result starts with `OK` and `A01` is a `GENERAL` attraction

#### Scenario: Invalid creation
- **WHEN** the identifier already exists or is blank, the name is blank, the capacity is `0`, `-5`, `veinte` or `2.5`, the type is unknown (for example `VIP`), or the parameter count is not three or four
- **THEN** the result starts with `ERROR` and no attraction is created

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Coches;10;GROUP` is processed
- **THEN** the result starts with `OK` and `A02` is a `GROUP` attraction

### Requirement: Stored attraction definitions
Attraction definitions SHALL be stored in a UTF-8 text file, one per line as `Id;Nombre;Capacidad;Tipo`, rewritten after each successful creation and loaded at startup. A stored line without the type field (`Id;Nombre;Capacidad`, written by earlier versions) SHALL be loaded as a `GENERAL` attraction. Present users SHALL NOT be stored: every run starts with all attractions empty. A missing file SHALL mean no attractions.

#### Scenario: Restart
- **GIVEN** `A01` was created and Ana entered it in a previous run
- **WHEN** the application restarts
- **THEN** `A01` exists with the same name, capacity and type and no users present, and the file contains `A01;Tornado;20;GENERAL`

#### Scenario: Legacy line
- **GIVEN** the attractions file contains the line `A09;Noria;5`
- **WHEN** the application starts
- **THEN** `A09` is loaded as a `GENERAL` attraction with capacity 5
