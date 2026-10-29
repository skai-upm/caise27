# Spec Delta

## ADDED Requirements

### Requirement: Attraction types and passes persisted
Each attraction's type SHALL be persisted, and attraction records without a type SHALL load as GENERAL. Every SpeedPass and Special Pass SHALL be persisted with its identifier, individual ticket, stored price and consumed state, including consumed passes, while its ticket exists. Passes SHALL be reconstructed at start-up. `--fly` mode SHALL cover these elements through the same repositories with different paths.

#### Scenario: Reload passes and types
- **WHEN** a SPECIAL attraction, a SpeedPass package with one consumed pass, and a Special Pass exist, and the application is restarted
- **THEN** the attraction is SPECIAL, and every pass exists with the same ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 loads as a GENERAL attraction
