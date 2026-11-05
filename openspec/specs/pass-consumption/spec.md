# pass-consumption Specification

## Purpose
Defines how passes are used and consumed when entering attractions: the optional `SPEED` variant of `ATTRACTION ENTER` for individual and group tickets, the indivisible group entry with SpeedPasses, and the automatic consumption of Special Passes at special attractions, always without modifying any pass when the entry fails.

## Requirements

### Requirement: Entry command with the optional SPEED variant
`ATTRACTION ENTER;<attraction>;<ticket>` SHALL keep its Delivery 2 behaviour, and the variant `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL request entry using SpeedPasses. Both forms SHALL always check the previous conditions (attraction and ticket exist, the ticket is valid for the current date, the attraction type accepts the ticket, there is capacity for everyone entering) plus the pass conditions below, all before changing any state. A fourth element other than `SPEED`, or more elements, SHALL be rejected with an `ERROR` response.

#### Scenario: Invalid option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an `ERROR` response is printed, nobody enters and no pass changes

#### Scenario: Normal entry consumes no SpeedPass
- **GIVEN** ticket `T001` has unconsumed SpeedPass `SP01:1`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed for a `GENERAL` attraction
- **THEN** the user enters and `SP01:1` is still unconsumed

### Requirement: Individual entry with a SpeedPass
When an individual ticket enters with `SPEED` an attraction that accepts SpeedPasses (`GENERAL` or `INDIVIDUAL`), the ticket SHALL have at least one unconsumed SpeedPass; on successful entry exactly one of its unconsumed SpeedPasses SHALL be marked as consumed and the response SHALL name the consumed pass. If the entry fails for any reason, no SpeedPass SHALL be modified.

#### Scenario: Exactly one SpeedPass consumed
- **GIVEN** ticket `T001`, valid today, has unconsumed SpeedPasses `SP02:1` to `SP02:10`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the response starts with `OK` and names the consumed SpeedPass
- **AND** exactly one of `SP02:1` to `SP02:10` is consumed

#### Scenario: No available SpeedPass
- **GIVEN** ticket `T002` has no unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T002;SPEED` is processed
- **THEN** an `ERROR` response is printed and nobody enters

#### Scenario: Failed entry leaves the SpeedPass unconsumed
- **GIVEN** ticket `T001` has unconsumed SpeedPass `SP01:1` and attraction `A02` is full
- **WHEN** `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** an `ERROR` response is printed and `SP01:1` is still unconsumed

### Requirement: Indivisible group entry with SpeedPasses
A group ticket MAY request entry with `SPEED` at `GENERAL` and `GROUP` attractions. Before changing any state the system SHALL check that the group ticket is valid for the current date, that the attraction type accepts group tickets, that there is capacity for all entering members, and that each individual ticket of the composition whose holder enters has an unconsumed SpeedPass. Entry and consumption SHALL be indivisible: if any condition fails nobody enters and no SpeedPass is consumed; otherwise all those members enter and exactly one SpeedPass of each of their individual tickets is consumed, and the response names the consumed passes.

#### Scenario: Whole group enters consuming one SpeedPass each
- **GIVEN** group ticket `TG01` for today with component tickets for Ana and Pedro, each with unconsumed SpeedPasses
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed for `GROUP` attraction `A03`
- **THEN** both enter and exactly one SpeedPass of each component ticket is consumed

#### Scenario: One member without SpeedPass blocks everyone
- **GIVEN** only Ana's component ticket of `TG01` has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an `ERROR` response is printed, neither Ana nor Pedro enters, and Ana's SpeedPass is still unconsumed

#### Scenario: Insufficient capacity consumes nothing
- **GIVEN** both component tickets of `TG01` have unconsumed SpeedPasses and the attraction has room for only one user
- **WHEN** `ATTRACTION ENTER;<attraction>;TG01;SPEED` is processed
- **THEN** an `ERROR` response is printed, nobody enters and no SpeedPass is consumed

### Requirement: Special Pass consumption at special attractions
Entry to a `SPECIAL` attraction SHALL use the normal syntax; the system SHALL automatically find the Special Pass of the individual ticket, check that it is unconsumed, and mark it as consumed only when the entry completes successfully, stating in the response that the Special Pass was consumed. If the ticket has no Special Pass, the pass is already consumed, or any other condition fails, the entry SHALL be rejected without changing the pass. The `SPEED` variant SHALL NOT be permitted at special attractions, and group tickets SHALL NOT enter special attractions.

#### Scenario: Special Pass consumed on entry
- **GIVEN** ticket `T001`, valid today, with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed for `SPECIAL` attraction `A04`
- **THEN** the response starts with `OK` and states that Special Pass `PE01` was consumed
- **AND** `PE01` is consumed, so a later entry of `T001` into `A04` is rejected

#### Scenario: SPEED not permitted at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an `ERROR` response is printed, nobody enters and no pass changes

#### Scenario: Failed special entry keeps the Special Pass
- **GIVEN** `SPECIAL` attraction `A04` is full and `T001` has unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an `ERROR` response is printed and `PE01` is still unconsumed
