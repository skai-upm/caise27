# attractions Specification

## Purpose

Defines park attractions — unique identifier, name, maximum capacity and the users currently inside — and the commands to create them and list their occupants.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an empty attraction when the identifier is not already used by another attraction, the name is non-empty and the capacity is a positive whole number.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both respond `ATTRACTION CREATED` and the attractions exist with capacities 20 and 12 and no users inside

#### Scenario: Duplicate identifier
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error is reported and `A01` is unchanged

#### Scenario: Invalid capacity
- **WHEN** the capacity is `0`, `-3`, `abc`, `2.5` or missing
- **THEN** an error is reported and no attraction is created

### Requirement: Unidirectional occupancy
An attraction SHALL hold references to the users currently inside it, and users SHALL NOT hold references to attractions.

#### Scenario: User has no attraction reference
- **WHEN** the user type is inspected
- **THEN** it has no attribute referring to attractions

### Requirement: Listing occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or a controlled empty result when nobody is inside, without changing the attraction's state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed and nobody is inside `A01`
- **THEN** the response is `No users in A01`

#### Scenario: Occupied attraction
- **WHEN** Ana and Pedro are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** the response lists `ana@correo.com` and `pedro@correo.com`, and the occupants are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed
- **THEN** an error is reported
