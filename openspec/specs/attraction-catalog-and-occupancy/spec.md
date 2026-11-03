# attraction-catalog-and-occupancy Specification

## Purpose

Adds park attractions with identity, name, capacity and an in-memory collection of users currently inside, plus a read-only way to list them (source F2-C05, section 6).

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a positive integer maximum capacity and no users inside; a duplicate identifier or a non-positive or non-integer capacity SHALL be rejected.

#### Scenario: Attraction created
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is reported and no attraction is created

#### Scenario: Duplicate identifier rejected
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed and `A01` exists
- **THEN** an error is reported and `A01` is unchanged

### Requirement: Attraction knows its occupants
An attraction SHALL hold references to the users currently inside it, and users SHALL NOT hold references to attractions.

#### Scenario: Unidirectional occupancy
- **WHEN** the source code is inspected
- **THEN** the attraction class holds a collection of users and the user class has no attraction reference

### Requirement: List attraction occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or an empty-equivalent result when there are none, without changing any state; an unknown attraction SHALL be reported as an error.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed for a newly created attraction
- **THEN** a controlled empty result is shown

#### Scenario: Occupied attraction
- **WHEN** Ana and Pedro are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** both emails are shown and occupancy is unchanged
