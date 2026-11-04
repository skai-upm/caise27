# attractions Specification

## Purpose
Defines the park's attractions — identifier, name, maximum capacity and the users currently inside — how they are created, and how their definitions persist between executions.

## Requirements

### Requirement: Attraction data
Each attraction SHALL have an identifier, a name, a maximum capacity and a collection of the registered users currently present in it.

#### Scenario: New attraction is empty
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` with capacity 20 exists with no users present

### Requirement: Create attraction
The system SHALL process `ATTRACTION CREATE;Identificador;Nombre;Aforo`, creating the attraction when the identifier is non-blank and unused, the name is non-blank and the capacity is a positive integer, and answering with an `OK:` response.

#### Scenario: Successful creation
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** an `OK:` response is printed

#### Scenario: Invalid creation
- **WHEN** `ATTRACTION CREATE;A01;Otra;10` is processed while `A01` exists, or the capacity is `0`, `-3`, `diez` or missing, or the name is blank
- **THEN** an `ERROR:` response is printed and no attraction is created or changed

### Requirement: Attraction definitions persist, occupancy does not
The system SHALL store each attraction's identifier, name and capacity in `attractions.txt` (`Identificador;Nombre;Aforo`, one per line, next to the other data files) after each creation and restore them at startup; the users present SHALL NOT be persisted, so every execution starts with all attractions empty.

#### Scenario: Restart
- **GIVEN** attraction `A01` was created and Ana entered it, and the application stopped
- **WHEN** the application starts again
- **THEN** `A01` exists with the same name and capacity and no users present

### Requirement: Attraction commands in HELP
The `HELP` response SHALL include `ATTRACTION CREATE;Identificador;Nombre;Aforo`, `ATTRACTION ENTER;Identificador;Ticket`, `ATTRACTION EXIT;Identificador;Ticket` and `ATTRACTION STATUS;Identificador`.

#### Scenario: Help shows attraction commands
- **WHEN** `HELP` is processed
- **THEN** the response contains the four lines
