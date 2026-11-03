# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity` and `ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity, a type (GENERAL when omitted) and no users inside; a duplicate identifier, a non-positive or non-integer capacity, or an unknown type SHALL be rejected.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is reported and no attraction is created

#### Scenario: Duplicate identifier rejected
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed and `A01` exists
- **THEN** an error is reported and `A01` is unchanged
