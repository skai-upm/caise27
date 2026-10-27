# Spec Delta

## ADDED Requirements

### Requirement: Attraction type persisted
The type of each attraction SHALL be stored and restored; stored attractions without a type SHALL be interpreted as GENERAL.

#### Scenario: Type survives restart
- **WHEN** SPECIAL attraction `A04` is created, the application ends and starts again
- **THEN** `A04` is a SPECIAL attraction

#### Scenario: Delivery 2 data
- **WHEN** the attractions file contains the line `A01;Dragon Rojo;20`
- **THEN** `A01` is loaded as a GENERAL attraction with capacity 20
