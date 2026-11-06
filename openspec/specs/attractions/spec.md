# attractions Specification

## Purpose

Defines the attractions of the Amusement Park — their identity, name, maximum capacity and the users currently inside — and how their current occupants are consulted.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with that unique identifier, non-empty name and maximum capacity (a positive whole number of people that may be inside simultaneously), with no users inside. A used identifier, an empty name or a capacity that is not a positive whole number MUST be rejected with an error and create nothing.

#### Scenario: Attraction created empty
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is printed and attraction `A01` named `Dragon Rojo` with capacity `20` exists with no users inside

#### Scenario: Duplicate identifier rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error reporting that the attraction already exists is printed and `A01` is unchanged

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A02;Torre del Cielo;0`, `...;-3` or `...;doce` is processed
- **THEN** an error reporting an invalid capacity is printed and no attraction is created

### Requirement: Occupants referenced only by the attraction
An attraction SHALL hold references to the users currently inside it, and users MUST NOT hold any reference to the attraction they are in.

#### Scenario: Occupants available from the attraction
- **GIVEN** user Ana is inside attraction `A01`
- **WHEN** the attraction's occupants are consulted
- **THEN** Ana's user is among them, and nothing about the attraction is stored in Ana's user data

### Requirement: List attraction occupants
`ATTRACTION LIST;AttractionIdentifier` SHALL print the email addresses of the users currently inside the attraction; when nobody is inside it MUST print a controlled empty result. The command is read-only and MUST NOT change the attraction. An unknown identifier MUST produce an error.

#### Scenario: Empty attraction
- **GIVEN** attraction `A01` with nobody inside
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** a message stating that `A01` has no users inside is printed and the attraction's occupants are unchanged

#### Scenario: Occupied attraction
- **GIVEN** Ana and Pedro are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed
- **THEN** an error reporting that there is no attraction `A99` is printed
