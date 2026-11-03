# attraction-status Specification

## Purpose

Shows the actual state of an attraction at the moment of the query, regardless of how its visitors entered.

## Requirements

### Requirement: Attraction status query
The command `ATTRACTION STATUS;IdAtraccion` SHALL display the attraction identifier, name, maximum capacity, number of users present, number of available places and the users present, counting every present user once whether they entered with an individual or a group ticket. An unknown attraction SHALL be rejected.

#### Scenario: Mixed entries
- **GIVEN** `A01` (Tornado, capacity 20), `ATTRACTION ENTER;A01;T001` for Ana and `ATTRACTION ENTER;A01;TG01` for Pedro and Luis
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** it shows `A01`, `Tornado`, capacity 20, 3 users present, 17 available places and Ana, Pedro and Luis

#### Scenario: Status after exit
- **GIVEN** the same state
- **WHEN** `ATTRACTION EXIT;A01;T001` and then `ATTRACTION STATUS;A01` are processed
- **THEN** it shows 2 users present, 18 available places and only Pedro and Luis

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is reported
