# Spec Delta

## Purpose

Defines the park's attractions: their identity, name and maximum capacity, the users currently inside each one, how users enter and leave them, and how their contents and status are consulted.

## ADDED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name and a maximum capacity — the maximum number of people inside simultaneously, a positive integer — and no users inside.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both are confirmed and attractions `A01` (capacity 20) and `A02` (capacity 12) exist and are empty

#### Scenario: Duplicate identifier
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error is printed and `A01` is unchanged

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0`, `ATTRACTION CREATE;A03;Noria;-2`, `ATTRACTION CREATE;A03;Noria;abc` or `ATTRACTION CREATE;A03;Noria` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Occupants kept by the attraction
- **WHEN** a user is inside attraction `A01`
- **THEN** the user is found by consulting `A01`'s occupants, and the user object has no attraction attribute

### Requirement: Attraction list
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users inside the attraction at that moment, or a controlled empty result when there are none, without changing the attraction's state.

#### Scenario: Empty attraction
- **WHEN** `A01` has no users inside and `ATTRACTION LIST;A01` is processed
- **THEN** a message stating that there are no users inside `A01` is printed

#### Scenario: Users listed
- **WHEN** Ana and Pedro are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed and both remain inside

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no such attraction exists
- **THEN** an error is printed
