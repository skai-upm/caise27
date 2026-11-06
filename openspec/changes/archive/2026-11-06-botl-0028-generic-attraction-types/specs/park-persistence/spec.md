# Spec Delta

## MODIFIED Requirements

### Requirement: Attractions persisted without occupants
Attractions SHALL be stored with their identifier, name, capacity and type and reconstructed at start-up; a stored attraction without a type MUST be loaded as `GENERAL`. The users inside attractions MUST NOT be persisted, so every attraction starts each execution empty.

#### Scenario: Attraction recovered empty
- **GIVEN** attraction `A01` (`Dragon Rojo`, capacity 20) had Ana inside when the previous execution ended
- **WHEN** the application starts again
- **THEN** `A01` exists with the same name and capacity and `ATTRACTION LIST;A01` reports no users inside

#### Scenario: Type recovered and legacy record defaulted
- **GIVEN** the attractions file contains `A04;Pasaje del Terror;15;SPECIAL` and the Delivery 2 line `A01;Dragon Rojo;20`
- **WHEN** the application starts
- **THEN** `A04` is `SPECIAL` and `A01` is `GENERAL`
