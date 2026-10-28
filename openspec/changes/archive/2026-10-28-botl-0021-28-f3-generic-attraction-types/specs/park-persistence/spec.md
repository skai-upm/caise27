## ADDED Requirements

### Requirement: Attraction type persisted
The type of each attraction SHALL be stored and restored; stored attractions without a type SHALL be loaded as GENERAL attractions.

#### Scenario: Typed attraction after restart
- **WHEN** a SPECIAL attraction is created and the application restarts
- **THEN** the attraction is still SPECIAL

#### Scenario: Data without type
- **WHEN** the attraction file contains a line `A01;Dragon Rojo;20` without type
- **THEN** `A01` is loaded as a GENERAL attraction
