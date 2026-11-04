# Spec Delta

## Purpose

Defines the park's attractions (identifier, name, maximum capacity and present users), how they are created, and how their definitions are kept between runs.

## ADDED Requirements

### Requirement: Attraction data
Each attraction SHALL have an identifier, a name, a maximum capacity, and a collection of the users currently present in it. Present users SHALL be the same user objects managed by the system, each counted once.

#### Scenario: New attraction is empty
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` succeeds
- **THEN** attraction `A01` named `Tornado` has capacity 20 and no users present

### Requirement: Create attraction
The system SHALL process `ATTRACTION CREATE;Id;Nombre;Capacidad`, creating the attraction only if the identifier and name are non-blank, the identifier is not used by another attraction, and the capacity is a positive integer written with digits only. Otherwise it SHALL print a line starting with `ERROR` and create nothing.

#### Scenario: Create
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed and no attraction `A01` exists
- **THEN** the result starts with `OK`

#### Scenario: Invalid creation
- **WHEN** the identifier already exists or is blank, the name is blank, the capacity is `0`, `-5`, `veinte` or `2.5`, or the parameter count is not three
- **THEN** the result starts with `ERROR` and no attraction is created

### Requirement: Stored attraction definitions
Attraction definitions SHALL be stored in a UTF-8 text file, one per line as `Id;Nombre;Capacidad`, rewritten after each successful creation and loaded at startup. Present users SHALL NOT be stored: every run starts with all attractions empty. A missing file SHALL mean no attractions.

#### Scenario: Restart
- **GIVEN** `A01` was created and Ana entered it in a previous run
- **WHEN** the application restarts
- **THEN** `A01` exists with the same name and capacity and no users present, and the file contains `A01;Tornado;20`
