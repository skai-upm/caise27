# Spec Delta

## ADDED Requirements

### Requirement: Individual entry with a SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at an attraction that accepts SpeedPasses SHALL require at least one unconsumed SpeedPass of that ticket, besides all normal entry conditions; on success exactly one of its unconsumed SpeedPasses SHALL be marked consumed and the output SHALL name it. If entry fails for any reason, no SpeedPass SHALL be modified.

#### Scenario: Entry consumes one SpeedPass
- **WHEN** `T001` (valid today) has unconsumed SpeedPasses `SP02:1`..`SP02:10` and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, exactly one SpeedPass is consumed, and the output names it

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is shown and nobody enters

#### Scenario: Failure leaves passes untouched
- **WHEN** `T001` has SpeedPasses but the attraction is full
- **THEN** entry is rejected and no SpeedPass is consumed

### Requirement: Group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at GENERAL and GROUP attractions SHALL check, before changing any state, that the group ticket is valid today, the attraction accepts group tickets, there is capacity for all members, and every composed individual ticket has an unconsumed SpeedPass. Entry and consumption SHALL be indivisible: on success all members enter and exactly one SpeedPass of each composed ticket is consumed; on any failure nobody enters and no SpeedPass is consumed.

#### Scenario: Whole group with SpeedPasses
- **WHEN** both composed tickets of `TG01` have an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed at GROUP attraction `A03`
- **THEN** both members enter and one SpeedPass of each composed ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** only one composed ticket of `TG01` has an unconsumed SpeedPass
- **THEN** entry is rejected, nobody enters and no SpeedPass is consumed

### Requirement: Special Pass consumed on entry
Normal entry to a SPECIAL attraction SHALL automatically use the ticket's Special Pass, which SHALL be unconsumed, and SHALL mark it consumed only when entry succeeds, indicating this in the output. If the ticket has no Special Pass, it is already consumed, or another condition fails, entry SHALL be rejected without changing the pass. The `;SPEED` variant and group tickets SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction `A04`
- **THEN** the user enters, `PE01` is consumed and the output says so

#### Scenario: Second entry with the consumed pass
- **WHEN** the user has left `A04` and `ATTRACTION ENTER;A04;T001` is processed again
- **THEN** entry is rejected because the Special Pass is consumed

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error message is shown, nobody enters and `PE01` is unchanged
