# attraction-catalog Specification

## Purpose

Defines the park's attractions, their identifying data and capacity, and how their definitions are kept across executions.

## Requirements

### Requirement: Attraction creation
The command `ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` SHALL create an attraction with a non-blank unused identifier, a non-blank name, a maximum capacity that is a positive integer and a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`; without `Tipo` it SHALL create a `GENERAL` attraction. It SHALL start with no users present. Other values, an unknown type or a wrong parameter count SHALL be rejected.

#### Scenario: Create an attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** a success response is returned and `A01` named `Tornado` exists with capacity 20 and no users present

#### Scenario: Invalid creation
- **WHEN** an ATTRACTION CREATE reuses `A01`, has an empty name, a capacity of `0`, `-3` or `abc`, or misses a parameter
- **THEN** an error is reported and no attraction is created

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Coches;10;GROUP` is processed
- **THEN** `A02` is created as a `GROUP` attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A03;X;10;VIP` is processed
- **THEN** an error is reported and no attraction is created

### Requirement: Attraction persistence
The system SHALL store attraction definitions in a text file, one per line as `Id;Nombre;Capacidad;Tipo`, save it after every successful creation, and load it at startup; a stored line without `Tipo` (written by earlier versions) SHALL be loaded as `GENERAL`. Users present SHALL NOT be stored: every attraction is empty when the application starts.

#### Scenario: Restart keeps definitions and empties attractions
- **GIVEN** a previous run created `A01` and admitted users into it
- **WHEN** the application restarts
- **THEN** `A01` exists with the same name and capacity and no users present, and the file contains `A01;Tornado;20;GENERAL`

#### Scenario: Earlier file without type
- **GIVEN** an attraction file line `A09;Antigua;5`
- **WHEN** the application starts
- **THEN** `A09` is a `GENERAL` attraction
