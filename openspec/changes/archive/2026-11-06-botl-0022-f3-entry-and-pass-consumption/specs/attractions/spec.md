# Spec Delta

## MODIFIED Requirements

### Requirement: Special attractions require a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only if that ticket has an unconsumed Special Pass. The pass SHALL be used automatically, without any extra option in the entry command, and SHALL be marked consumed only when the entry completes, with the output stating that it was consumed. If the ticket has no Special Pass, the pass is already consumed, or any other condition fails, the entry SHALL be rejected and the pass SHALL NOT change. The `SPEED` variant SHALL NOT be allowed at SPECIAL attractions.

#### Scenario: No Special Pass
- **GIVEN** SPECIAL attraction `A04` and a valid ticket `T001` without a Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error message is displayed and nobody enters

#### Scenario: With Special Pass
- **GIVEN** ticket `T001` has unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the user enters `A04`

#### Scenario: Special Pass consumed on entry
- **GIVEN** ticket `T001` has unconsumed Special Pass `PE01` and SPECIAL attraction `A04` has room
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the output states that `PE01` was consumed and `PE01` is now consumed
- **AND** after the user leaves, a second `ATTRACTION ENTER;A04;T001` is rejected

#### Scenario: Failed special entry keeps the pass
- **GIVEN** `A04` is full and `T001` has unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error message is displayed and `PE01` is still unconsumed

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error message is displayed and no pass changes

## ADDED Requirements

### Requirement: Individual entry with a SpeedPass
The system SHALL, for `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at a GENERAL or INDIVIDUAL attraction, require at least one unconsumed SpeedPass for the ticket in addition to every normal entry condition. On success, exactly one of those SpeedPasses SHALL be marked consumed and the output SHALL name it. If the entry fails for any reason, no SpeedPass SHALL change.

#### Scenario: SpeedPass consumed
- **GIVEN** ticket `T001` with unconsumed SpeedPasses `SP01:1` and `SP02:1`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, the output names the consumed pass, and exactly one of the two is consumed

#### Scenario: No available SpeedPass
- **GIVEN** ticket `T001` whose SpeedPasses are all consumed
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is displayed and nobody enters

#### Scenario: Failed entry keeps SpeedPasses
- **GIVEN** attraction `A01` is full and `T001` has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is displayed and the SpeedPass is still unconsumed

### Requirement: Group entry with SpeedPasses
The system SHALL, for `ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at a GENERAL or GROUP attraction, verify before changing anything that the group ticket is valid today, that the attraction accepts group tickets, that capacity suffices for all members, and that every composed individual ticket has an unconsumed SpeedPass. If all conditions hold, all members SHALL enter and exactly one SpeedPass of each composed ticket SHALL be consumed. Otherwise no member SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: Whole group enters with SpeedPasses
- **GIVEN** group ticket `TG01` whose two composed tickets each have an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both members enter and one SpeedPass of each composed ticket is consumed

#### Scenario: One member lacks a SpeedPass
- **GIVEN** group ticket `TG01` where only one composed ticket has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an error message is displayed, nobody enters, and that SpeedPass is still unconsumed

### Requirement: Entry option validation and HELP
The fourth argument of `ATTRACTION ENTER`, when present, SHALL be exactly `SPEED`, and any other value SHALL be rejected with an error message. `HELP` SHALL include `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: Unknown entry option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error message is displayed and nobody enters
