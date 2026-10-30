# Spec Delta

## MODIFIED Requirements

### Requirement: Park state persisted in text files
Groups with their members, individual tickets with their user, group tickets with their composition and group reference, attractions with their type, and passes with their ticket, stored price and consumed state SHALL be stored in text files (no Java serialization or databases; no libraries other than BCrypt) and reconstructed at start-up with the same relationships. Attraction lines without a type SHALL be read as GENERAL.

#### Scenario: State survives restart
- **WHEN** users, a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, compositions, prices, dates and group reference

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction, a consumed Special Pass and two SpeedPasses (one consumed) exist and the application is restarted
- **THEN** the attraction is SPECIAL and the passes exist with the same tickets, prices and consumed states

#### Scenario: Delivery 2 attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction
