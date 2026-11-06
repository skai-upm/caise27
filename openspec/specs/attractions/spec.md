# attractions Specification

## Purpose
Defines the park's attractions, each with a unique identifier, a name, a maximum capacity and the users currently inside it, and the commands to create attractions and list their occupants.

## Requirements

### Requirement: Create an attraction
The system SHALL create an attraction with `ATTRACTION CREATE;Identifier;Name;Capacity` when the identifier and name are non-empty, the identifier is not used by another attraction, and the capacity is a positive whole number. The new attraction SHALL have no occupants. Otherwise it SHALL reject the command with an error message and create nothing.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is displayed
- **AND** attraction `A01` named `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid attraction is rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;5`, `ATTRACTION CREATE;A02;Torre;0`, `ATTRACTION CREATE;A02;Torre;-3`, `ATTRACTION CREATE;A02;Torre;doce`, `ATTRACTION CREATE;A02;;12` or `ATTRACTION CREATE;A02;Torre` is processed
- **THEN** an error message is displayed
- **AND** `A01` is unchanged and `A02` does not exist

### Requirement: Attraction occupants are referenced one way
An attraction SHALL hold the collection of users currently inside it, each at most once. Users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Occupants known only by the attraction
- **GIVEN** a user is inside attraction `A01`
- **WHEN** the occupants of `A01` are consulted
- **THEN** the user is included
- **AND** the user's own data does not refer to `A01`

### Requirement: List attraction occupants
The system SHALL display, for `ATTRACTION LIST;AttractionIdentifier`, the email address of each user currently inside the attraction. When it has no occupants, the result SHALL be a controlled empty result. The operation SHALL NOT change the attraction. An unknown identifier SHALL produce an error message.

#### Scenario: Empty attraction
- **GIVEN** attraction `A01` has no occupants
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** a message states that there are no users in `A01`
- **AND** `A01` still has no occupants and the same capacity

#### Scenario: Attraction with occupants
- **GIVEN** `ana@correo.com` and `pedro@correo.com` are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** both email addresses are displayed
- **AND** the occupants are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and `A99` does not exist
- **THEN** an error message is displayed

### Requirement: ATTRACTION commands listed in HELP
The system SHALL include `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>` and `ATTRACTION LIST;<AttractionIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both ATTRACTION command syntaxes
