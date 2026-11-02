# attraction-catalog-and-occupancy Specification

## Purpose

Defines park attractions with identity, name, maximum capacity and current occupants, and a read-only listing of who is inside.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an empty attraction with a unique identifier, a name and a maximum capacity that is a positive whole number. A duplicate identifier or an invalid capacity SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A03;X;0`, `ATTRACTION CREATE;A03;X;abc` or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** an error is reported and no attraction is created or changed

### Requirement: Occupant direction
An attraction SHALL hold references to the users currently inside it; users SHALL NOT hold references to attractions.

#### Scenario: User representation unaffected
- **WHEN** Ana is inside A01
- **THEN** A01's occupants include Ana and Ana's representation holds no attraction reference

### Requirement: ATTRACTION LIST
`ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, produce an empty or equivalent result in a controlled way when there are none, and SHALL NOT change any state.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A01` is processed for a new attraction
- **THEN** an empty or equivalent result is displayed

#### Scenario: Occupied attraction
- **WHEN** Ana and Pedro are inside A01 and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are displayed and occupancy is unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and A99 does not exist
- **THEN** an error is reported
