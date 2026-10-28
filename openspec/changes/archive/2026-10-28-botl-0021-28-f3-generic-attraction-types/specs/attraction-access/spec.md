## ADDED Requirements

### Requirement: Entry restricted by attraction type
`ATTRACTION ENTER` SHALL admit individual and group tickets in GENERAL attractions, only individual tickets in INDIVIDUAL and SPECIAL attractions, and only group tickets in GROUP attractions; a ticket of a type the attraction does not accept SHALL be rejected with an error message and nobody enters.

#### Scenario: Group ticket at an individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction `A02` and a valid group ticket `TG01`
- **THEN** an error message is displayed and nobody enters `A02`

#### Scenario: Individual ticket at a group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction `A03` and a valid individual ticket `T001`
- **THEN** an error message is displayed and nobody enters `A03`

#### Scenario: Accepted ticket types enter
- **WHEN** valid tickets are used at attractions of a type that accepts them (individual at GENERAL and INDIVIDUAL, group at GENERAL and GROUP)
- **THEN** their users enter

### Requirement: Special attraction requires a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only when an unconsumed Special Pass is associated with that ticket; otherwise entry SHALL be rejected with an error message.

#### Scenario: Without Special Pass
- **WHEN** valid individual ticket `T001` has no unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed for SPECIAL attraction `A04`
- **THEN** an error message is displayed and nobody enters

#### Scenario: With Special Pass
- **WHEN** valid individual ticket `T001` has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the ticket's user enters `A04`
