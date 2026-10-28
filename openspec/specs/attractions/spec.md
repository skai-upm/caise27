# attractions Specification

## Purpose
Defines the park's attractions, each with identity, name, maximum capacity and the users currently inside, and how to consult those occupants.

## Requirements

### Requirement: ATTRACTION CREATE command
The system SHALL accept `ATTRACTION CREATE;Identifier;Name;Capacity` and create an attraction with that unique identifier, name and maximum capacity and no occupants. A duplicate identifier, an empty field, a capacity that is not a positive integer, or a wrong number of fields SHALL be rejected with an error message without creating an attraction.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** attractions `A01` (capacity 20) and `A02` (capacity 12) exist with no users inside

#### Scenario: Invalid attraction rejected
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` (duplicate), `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A04;Noria;abc` is processed
- **THEN** an error message is displayed for each and no new attraction is created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it, never more than its capacity and each user at most once; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Capacity is never exceeded
- **WHEN** an attraction with capacity 1 already holds one user and another user is added
- **THEN** the addition is rejected and the attraction still holds one user

### Requirement: ATTRACTION LIST command
The system SHALL accept `ATTRACTION LIST;AttractionIdentifier` and display the e-mail addresses of the users currently inside that attraction, or a controlled empty result when there are none, without changing the attraction's state. An unknown attraction SHALL be reported with an error message.

#### Scenario: List occupants
- **WHEN** users `ana@correo.com` and `pedro@correo.com` are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** both e-mail addresses are displayed and the occupants of `A01` are unchanged

#### Scenario: Empty attraction
- **WHEN** no user is inside `A02` and `ATTRACTION LIST;A02` is processed
- **THEN** an empty-result message is displayed without error

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no such attraction exists
- **THEN** an error message is displayed
