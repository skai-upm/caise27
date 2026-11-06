# Spec Delta

## ADDED Requirements

### Requirement: Attraction type is persisted
The system SHALL store each attraction's type and restore it at startup. A stored attraction without a type SHALL be loaded as a GENERAL attraction.

#### Scenario: Typed attraction survives restart
- **GIVEN** SPECIAL attraction `A04` was created
- **WHEN** the application restarts
- **THEN** `A04` is still SPECIAL

#### Scenario: Data from Delivery 2
- **GIVEN** a stored attraction line `A01;Dragon Rojo;20` without a type
- **WHEN** the application starts
- **THEN** `A01` is a GENERAL attraction with capacity 20
