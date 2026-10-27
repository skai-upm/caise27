# Spec Delta

## ADDED Requirements

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users inside, number of available places and the users inside, obtained from the attraction's own state and independent of how the users entered.

#### Scenario: Status after mixed entries
- **WHEN** `A01` (Dragon Rojo, capacity 20) contains Luis, who entered with an individual ticket, and Ana and Pedro, who entered with a group ticket
- **THEN** `ATTRACTION STATUS;A01` shows `A01`, `Dragon Rojo`, capacity 20, 3 users inside, 17 available places and the three emails

#### Scenario: Status of an empty attraction
- **WHEN** nobody is inside `A02` (capacity 12)
- **THEN** the status shows 0 users inside, 12 available places and no users

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is reported
