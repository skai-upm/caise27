# Spec Delta

## MODIFIED Requirements

### Requirement: Indivisible group entry
`ATTRACTION ENTER` with a group ticket valid for the current date at an attraction that accepts group tickets (GENERAL or GROUP) SHALL admit all users of its composed individual tickets together, through the same entry operation used for individual tickets, and SHALL admit none when free places are fewer than those users.

#### Scenario: Insufficient capacity for the whole group
- **WHEN** attraction `A02` has 3 free places and a valid group ticket for 4 users is used
- **THEN** an error is reported and none of the 4 users enters

#### Scenario: Whole group enters
- **WHEN** attraction `A01` has 20 free places and valid group ticket `TG01` for 2 users is used
- **THEN** both users are inside `A01`

#### Scenario: Group ticket at a SPECIAL attraction
- **WHEN** a valid group ticket is used at a SPECIAL attraction
- **THEN** an error is reported and nobody enters
