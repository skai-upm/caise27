# Spec Delta

## ADDED Requirements

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL, in addition to all normal entry conditions, require at least one unconsumed SpeedPass for each individual ticket involved (the ticket itself, or every composed ticket of a group ticket). If entry succeeds, exactly one unconsumed SpeedPass per individual ticket SHALL be marked consumed and the output SHALL indicate which passes were consumed. SPEED SHALL be accepted only at GENERAL and INDIVIDUAL attractions for individual tickets, and at GENERAL and GROUP attractions for group tickets.

#### Scenario: Individual SpeedPass entry
- **WHEN** valid ticket `T001` has unconsumed SpeedPasses `SP01:1` and `SP02:1` and `ATTRACTION ENTER;A01;T001;SPEED` is processed at GENERAL attraction `A01`
- **THEN** Ana enters, exactly one of those passes is consumed and the output names it

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error is printed and Ana does not enter

#### Scenario: Group SpeedPass entry
- **WHEN** both composed tickets of valid group ticket `TG01` have an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed at GROUP attraction `A03`
- **THEN** both members enter and one SpeedPass of each composed ticket is consumed

#### Scenario: Group entry is indivisible
- **WHEN** only one composed ticket of `TG01` has an unconsumed SpeedPass, or the attraction lacks room for all members, and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is printed, nobody enters and no SpeedPass is consumed

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed at SPECIAL attraction `A04`
- **THEN** an error is printed, nobody enters and no pass changes

#### Scenario: Invalid option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error is printed

### Requirement: Special Pass consumption at special attractions
Entering a SPECIAL attraction with the normal syntax SHALL automatically find the ticket's Special Pass, require it to be unconsumed, and mark it consumed only when entry completes successfully, indicating this in the output. Without a Special Pass, with a consumed one, or when any other condition fails, entry SHALL be rejected without changing the pass.

#### Scenario: Special Pass consumed on entry
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed with room available
- **THEN** Ana enters, `PE01` becomes consumed and the output says so

#### Scenario: Consumed Special Pass rejected
- **WHEN** Ana has left `A04` and `ATTRACTION ENTER;A04;T001` is processed again with `PE01` consumed
- **THEN** an error is printed and Ana does not enter

#### Scenario: Full special attraction keeps the pass
- **WHEN** `A04` is full and `ATTRACTION ENTER;A04;T001` is processed with unconsumed `PE01`
- **THEN** an error is printed and `PE01` remains unconsumed

### Requirement: Failed entries leave passes unchanged
If an entry fails for any reason, no SpeedPass or Special Pass SHALL be modified.

#### Scenario: Wrong date keeps passes
- **WHEN** `T001` is not valid for the current date and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error is printed and all of `T001`'s passes remain unconsumed
