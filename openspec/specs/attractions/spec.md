# attractions Specification

## Purpose

Defines park attractions: their identity, name, maximum capacity and the collection of users currently inside them, plus creation and the read-only listing of occupants.

## Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a maximum capacity and an initially empty collection of users inside it. It SHALL be rejected with an error message when the identifier already exists, the identifier or name is blank, the capacity is not a whole number greater than zero, or the number of arguments is wrong.

#### Scenario: Create attractions from the source example
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both are confirmed, `A01` has name `Dragon Rojo` and capacity 20, `A02` has capacity 12, and both are empty

#### Scenario: Duplicate identifier rejected
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error message is shown and `A01` is unchanged

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;X;0`, `ATTRACTION CREATE;A03;X;-1` or `ATTRACTION CREATE;A03;X;abc` is processed
- **THEN** an error message is shown and `A03` is not created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it and SHALL never hold more users than its capacity or the same user twice; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Capacity bound respected
- **WHEN** an attraction with capacity 1 already holds one user and another user is added
- **THEN** the addition is refused and the attraction still holds one user

### Requirement: List attraction occupants
The command `ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or a controlled empty result when there are none, without changing the attraction's state. An unknown attraction SHALL be rejected with an error message.

#### Scenario: List occupants
- **WHEN** `ana@correo.com` and `pedro@correo.com` are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** both email addresses are displayed and `A01` still holds both users

#### Scenario: Empty attraction
- **WHEN** `A02` has no users inside and `ATTRACTION LIST;A02` is processed
- **THEN** an empty-result message is displayed and no error occurs

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no attraction `A99` exists
- **THEN** an error message is shown
