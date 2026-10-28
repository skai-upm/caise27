## ADDED Requirements

### Requirement: Entry with SpeedPass
The system SHALL accept `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` at GENERAL, INDIVIDUAL and GROUP attractions. All entry conditions of plain entry SHALL apply; in addition each individual ticket admitted SHALL have at least one unconsumed SpeedPass. On success exactly one available SpeedPass per individual ticket SHALL be marked consumed and the output SHALL name each consumed pass; on any failure no SpeedPass SHALL change. A fourth field other than `SPEED` SHALL be rejected with an error message.

#### Scenario: Individual entry consumes one SpeedPass
- **WHEN** valid ticket `T001` has unconsumed SpeedPasses `SP01:1` and `SP02:1` and `ATTRACTION ENTER;A01;T001;SPEED` is processed at GENERAL attraction `A01` with free capacity
- **THEN** the user enters, exactly one of those SpeedPasses is consumed and the output names it

#### Scenario: No SpeedPass available
- **WHEN** valid ticket `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is displayed, nobody enters and no pass changes

#### Scenario: Failed entry keeps passes
- **WHEN** `T001` has an unconsumed SpeedPass but attraction `A01` is full and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is displayed and the SpeedPass is still unconsumed

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error message is displayed and nobody enters

### Requirement: Indivisible group entry with SpeedPasses
For `ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` the system SHALL check, before changing any state, that the group ticket is valid today, the attraction accepts group tickets, there is capacity for all members and every individual ticket of the composition has an unconsumed SpeedPass. If all hold, all members SHALL enter and exactly one SpeedPass per individual ticket SHALL be consumed; otherwise nobody SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: Whole group with SpeedPasses
- **WHEN** every member ticket of valid group ticket `TG01` has an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed at GROUP attraction `A03` with enough capacity
- **THEN** all members enter and one SpeedPass of each member ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** one member ticket of `TG01` has no unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an error message is displayed, nobody enters and no SpeedPass of any member is consumed

### Requirement: Special Pass consumption at special attractions
At a SPECIAL attraction, `ATTRACTION ENTER;<attraction>;<ticket>` SHALL automatically use the ticket's unconsumed Special Pass and mark it consumed only when entry succeeds, indicating it in the output. Missing or consumed Special Passes, or any other failed condition, SHALL reject the entry without changing the pass. The `SPEED` variant and group tickets SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed on entry
- **WHEN** valid ticket `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction `A04`
- **THEN** the user enters, `PE01` is consumed and the output says so

#### Scenario: Second entry with the consumed pass
- **WHEN** `PE01` is already consumed and the user (having left) tries `ATTRACTION ENTER;A04;T001` again
- **THEN** an error message is displayed and nobody enters

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error message is displayed, nobody enters and no pass changes
