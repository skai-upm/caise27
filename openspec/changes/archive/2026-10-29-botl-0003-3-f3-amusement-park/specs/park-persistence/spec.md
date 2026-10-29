# Spec Delta

## MODIFIED Requirements

### Requirement: Park state persisted in text files
Groups with their users, individual tickets with their user, group tickets with their composition and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their ticket, stored price and consumed state SHALL be stored in text files and reconstructed at start-up. Attraction lines without a type MUST be read as GENERAL. Java object serialization, databases, persistence frameworks and external persistence libraries MUST NOT be used.

#### Scenario: State survives restart
- **WHEN** a group, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all four are reconstructed with the same identifiers, dates, prices, members, compositions and group reference

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction, a consumed Special Pass and unconsumed SpeedPasses exist and the application restarts
- **THEN** the attraction type, the passes, their tickets, prices and consumed states are reconstructed

#### Scenario: Delivery 2 attraction file
- **WHEN** `attractions.txt` contains `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction
