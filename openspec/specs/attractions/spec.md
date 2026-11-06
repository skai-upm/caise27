# attractions Specification

## Purpose

Defines the attractions of the Amusement Park — their identity, name, maximum capacity and the users currently inside — and how their current occupants are consulted.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an attraction with that unique identifier, non-empty name, maximum capacity (a positive whole number of people that may be inside simultaneously) and type (default `GENERAL` when omitted), with no users inside. A used identifier, an empty name or a capacity that is not a positive whole number MUST be rejected with an error and create nothing.

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

#### Scenario: Delivery 2 syntax creates a general attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction `A01` of type `GENERAL` is created

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

### Requirement: Leave an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, releasing one place, and print a confirmation. Users who entered together with a group ticket SHALL leave individually with their own email. An unknown attraction, or an email of nobody currently inside, MUST be rejected with an error and change nothing.

#### Scenario: User leaves and frees a place
- **GIVEN** Ana is inside `A03` (capacity 1)
- **WHEN** `ATTRACTION EXIT;A03;ana@correo.com` is processed
- **THEN** Ana is no longer inside and another valid ticket holder can enter `A03`

#### Scenario: Group members leave individually
- **GIVEN** Ana and Pedro entered `A01` with group ticket `TG01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only Pedro remains inside `A01`

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside
- **THEN** an error reporting that the user is not inside `A01` is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print the attraction's identifier, name, maximum capacity, number of users inside, number of available places and the users currently inside, all taken from the attraction's own state and independent of how users entered. It is read-only; an unknown identifier MUST produce an error.

#### Scenario: Status after mixed entries
- **GIVEN** attraction `A01` (`Dragon Rojo`, capacity 20) with Ana inside via an individual ticket and Pedro via a group ticket
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity `20`, `2` inside, `18` available and both emails

#### Scenario: Empty attraction status
- **GIVEN** attraction `A02` with nobody inside
- **WHEN** `ATTRACTION STATUS;A02` is processed
- **THEN** the output shows `0` inside, all places available and no users
