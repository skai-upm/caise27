# Spec Delta

## Purpose

Defines the attractions of the park: their identity, name, maximum capacity and the users currently inside, how they are created, and how their current occupants are listed.

## ADDED Requirements

### Requirement: Attraction data
Each attraction SHALL have a unique identifier, a name, a maximum capacity (the maximum number of people inside simultaneously) and a collection of the users currently inside. The attraction SHALL reference its occupants and users SHALL NOT reference the attraction they are in.

#### Scenario: New attraction is empty
- **WHEN** an attraction is created
- **THEN** it has the given identifier, name and capacity and no users inside

### Requirement: ATTRACTION CREATE command
The command `ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create the attraction and print `ATTRACTION CREATED`. An identifier already used by another attraction, an empty identifier or name, or a capacity that is not a whole number of at least 1 SHALL be rejected with an `ERROR:` message.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both print `ATTRACTION CREATED` and attractions `A01` (capacity 20) and `A02` (capacity 12) exist

#### Scenario: Invalid attraction
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed while `A01` exists, or the capacity is `0`, `-3`, `abc` or `2.5`
- **THEN** an `ERROR:` line is printed and no attraction is created or changed

### Requirement: ATTRACTION LIST command
The command `ATTRACTION LIST;AttractionIdentifier` SHALL print the email addresses of the users inside the attraction at that moment, one per line, or the line `NO USERS IN ATTRACTION <identifier>` when it is empty. It SHALL NOT change the attraction. An unknown identifier SHALL be rejected with an `ERROR:` message.

#### Scenario: Empty attraction
- **WHEN** `A01` has no users inside and `ATTRACTION LIST;A01` is processed
- **THEN** `NO USERS IN ATTRACTION A01` is printed

#### Scenario: Occupied attraction
- **WHEN** Ana and Pedro are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed and the occupants of `A01` are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no attraction `A99` exists
- **THEN** an `ERROR:` line is printed
