## MODIFIED Requirements

### Requirement: Persist park elements
Groups with their members, individual tickets with their user, group tickets with their composition and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their ticket, stored price and consumed state SHALL be stored in text files and reconstructed at start-up, without Java serialization, databases or external libraries other than BCrypt. Attraction occupants SHALL NOT be persisted; all attractions start empty. Attraction records without a type SHALL load as `GENERAL`.

#### Scenario: Reconstruct after restart
- **WHEN** groups, tickets and attractions are created and the application is restarted
- **THEN** they exist with the same members, compositions, prices and group references

#### Scenario: Attractions start empty
- **WHEN** users were inside an attraction when the application ended
- **THEN** after restart that attraction has no occupants

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction exists and a consumed Special Pass and unconsumed SpeedPasses exist, and the application restarts
- **THEN** the attraction is SPECIAL and the passes keep their ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 loads as a GENERAL attraction
