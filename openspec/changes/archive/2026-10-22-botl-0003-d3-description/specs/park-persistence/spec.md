# Spec Delta

## MODIFIED Requirements

### Requirement: Park data storage
Groups with their members, individual tickets with their user, group tickets with their individual tickets and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their individual ticket, stored price and consumed state SHALL be stored in text files in the data directory after every change, without Java serialization or databases, and reconstructed at startup with the same relationships. Attraction records without a type SHALL be read as `GENERAL`.

#### Scenario: State survives restart
- **WHEN** a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, users, composition, prices and dates

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction, a consumed Special Pass and an unconsumed SpeedPass exist and the application is restarted
- **THEN** the attraction is still SPECIAL and both passes exist with their ticket, price and consumed state

#### Scenario: Delivery 2 attraction data
- **WHEN** `attractions.txt` contains the line `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction
