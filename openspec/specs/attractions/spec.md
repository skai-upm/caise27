# attractions Specification

## Purpose
Defines the park's attractions — identifier, name, maximum capacity and the users currently present — and how they are created, stored and queried.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create an empty attraction of type `GENERAL`, and `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` an empty attraction of the given type (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`), when the identifier is nonblank and unused, the name is nonblank, and the capacity is a positive whole number written with digits only.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** an `OK` message is produced and attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Invalid creation
- **WHEN** the identifier is blank or already used, the name is blank, the capacity is `0`, `-3`, `veinte` or `2.5`, or the parameter count is wrong
- **THEN** an `ERROR` message is produced and no attraction is created or changed

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Saltamontes;10;GROUP` (or `GENERAL`, `INDIVIDUAL`, `SPECIAL`, case-insensitive) is processed
- **THEN** an `OK` message is produced and `A02` is an attraction of that type

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A03;X;10;VIP` is processed
- **THEN** an `ERROR` message is produced and no attraction is created

### Requirement: Attraction definitions persist, occupancy does not
Attraction identifiers, names, capacities and types SHALL be stored in a text file (`id;name;capacity;type`, one per line; lines without a type are read as `GENERAL`) after each successful creation and restored at startup; every execution SHALL start with all attractions empty.

#### Scenario: Restart
- **GIVEN** a previous execution created `A01` and admitted visitors
- **WHEN** the application starts again
- **THEN** `A01` exists with the same name, capacity and type and has no users present

#### Scenario: Legacy definitions without type
- **GIVEN** an attractions file line `A01;Tornado;20` written by an earlier version
- **WHEN** the application starts
- **THEN** `A01` is a `GENERAL` attraction with capacity 20

### Requirement: Attraction status
`ATTRACTION STATUS;IdAtraccion` SHALL display the attraction identifier, name, type, maximum capacity, number of users currently present, number of available places and the users currently present, reflecting the state at the moment of the command regardless of the kind of ticket used to enter. Unknown attractions SHALL be rejected.

#### Scenario: Status after mixed entries
- **GIVEN** `A01` with capacity 20, `T001` admitted Ana and `TG01` admitted Pedro and Luis
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Tornado`, type `GENERAL`, capacity 20, 3 present, 17 available and the three users, each listed once

#### Scenario: Status after exit
- **GIVEN** the previous state
- **WHEN** `ATTRACTION EXIT;A01;T001` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows 2 present and 18 available and no longer lists Ana

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an `ERROR` message is produced

### Requirement: Attraction types
The system SHALL support four attraction types with these accepted tickets: `GENERAL` (individual and group tickets), `INDIVIDUAL` (individual tickets only), `GROUP` (group tickets only) and `SPECIAL` (individual tickets only, as the basis for its Special Pass rules).

#### Scenario: Types exist
- **WHEN** one attraction of each type is created
- **THEN** each reports its type in `ATTRACTION STATUS`
