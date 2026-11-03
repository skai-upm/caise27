# atomic-group-entry-and-exit Specification

## Purpose

Extends attraction access to group tickets with all-or-nothing admission, and adds leaving attractions and checking their status.

## Requirements

### Requirement: Atomic group entry
`ATTRACTION ENTER` with a group ticket valid today SHALL admit together every user of its composed tickets, through the same access operation used for individual tickets; if capacity is insufficient for all of them, none SHALL enter.

#### Scenario: Group fits
- **WHEN** a group ticket of 3 members is used on an attraction with 3 free places
- **THEN** all 3 users enter

#### Scenario: Group does not fit
- **WHEN** a group ticket of 4 members is used on an attraction with capacity 3
- **THEN** no member enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and release one place; users who entered with a group ticket SHALL leave individually.

#### Scenario: One group member exits
- **WHEN** one of the users admitted through a group ticket exits
- **THEN** exactly one place is released and the others remain inside

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT` names a user who is not inside
- **THEN** an error is reported and occupancy is unchanged

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the identifier, name, maximum capacity, number of users inside, available places and the users inside, from the attraction's own state, regardless of how users entered.

#### Scenario: Status after entries and exits
- **WHEN** users entered with individual and group tickets and one exited
- **THEN** status shows the capacity, the current occupied and available counts and the present users
