# attraction-catalog Specification

## Purpose

Defines the park's attractions, their identifying data and capacity, and how their definitions are kept across executions.

## Requirements

### Requirement: Attraction creation
The command `ATTRACTION CREATE;Id;Nombre;Capacidad` SHALL create an attraction with a non-blank unused identifier, a non-blank name and a maximum capacity that is a positive integer; it SHALL start with no users present. Other values or a wrong parameter count SHALL be rejected.

#### Scenario: Create an attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** a success response is returned and `A01` named `Tornado` exists with capacity 20 and no users present

#### Scenario: Invalid creation
- **WHEN** an ATTRACTION CREATE reuses `A01`, has an empty name, a capacity of `0`, `-3` or `abc`, or misses a parameter
- **THEN** an error is reported and no attraction is created

### Requirement: Attraction persistence
The system SHALL store attraction definitions in a text file, one per line as `Id;Nombre;Capacidad`, save it after every successful creation, and load it at startup. Users present SHALL NOT be stored: every attraction is empty when the application starts.

#### Scenario: Restart keeps definitions and empties attractions
- **GIVEN** a previous run created `A01` and admitted users into it
- **WHEN** the application restarts
- **THEN** `A01` exists with the same name and capacity and no users present, and the file contains `A01;Tornado;20`
