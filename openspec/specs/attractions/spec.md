# attractions Specification

## Purpose
Defines the park's attractions — identifier, name, maximum capacity and the users currently present — and how they are created, stored and queried.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create an empty attraction when the identifier is nonblank and unused, the name is nonblank, and the capacity is a positive whole number written with digits only.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** an `OK` message is produced and attraction `A01` named `Tornado` with capacity 20 and no users present exists

#### Scenario: Invalid creation
- **WHEN** the identifier is blank or already used, the name is blank, the capacity is `0`, `-3`, `veinte` or `2.5`, or the parameter count is wrong
- **THEN** an `ERROR` message is produced and no attraction is created or changed

### Requirement: Attraction definitions persist, occupancy does not
Attraction identifiers, names and capacities SHALL be stored in a text file (`id;name;capacity`, one per line) after each successful creation and restored at startup; every execution SHALL start with all attractions empty.

#### Scenario: Restart
- **GIVEN** a previous execution created `A01` and admitted visitors
- **WHEN** the application starts again
- **THEN** `A01` exists with the same name and capacity and has no users present

### Requirement: Attraction status
`ATTRACTION STATUS;IdAtraccion` SHALL display the attraction identifier, name, maximum capacity, number of users currently present, number of available places and the users currently present, reflecting the state at the moment of the command regardless of the kind of ticket used to enter. Unknown attractions SHALL be rejected.

#### Scenario: Status after mixed entries
- **GIVEN** `A01` with capacity 20, `T001` admitted Ana and `TG01` admitted Pedro and Luis
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Tornado`, capacity 20, 3 present, 17 available and the three users, each listed once

#### Scenario: Status after exit
- **GIVEN** the previous state
- **WHEN** `ATTRACTION EXIT;A01;T001` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the status shows 2 present and 18 available and no longer lists Ana

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an `ERROR` message is produced
