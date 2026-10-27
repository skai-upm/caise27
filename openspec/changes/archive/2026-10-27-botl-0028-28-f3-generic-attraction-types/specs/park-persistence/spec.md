# Spec Delta

## ADDED Requirements

### Requirement: Attraction type persisted
The attraction type SHALL be stored with each attraction, and stored attractions without a type SHALL be loaded as GENERAL.

#### Scenario: Typed attraction survives restart
- **WHEN** SPECIAL attraction `A04` is created and the application restarts
- **THEN** `A04` is still a SPECIAL attraction

#### Scenario: Delivery 2 data
- **WHEN** the attractions file contains the line `A01;Dragon Rojo;20`
- **THEN** `A01` is loaded as a GENERAL attraction
