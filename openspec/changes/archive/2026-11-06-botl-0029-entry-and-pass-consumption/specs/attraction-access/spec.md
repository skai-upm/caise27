# Spec Delta

## ADDED Requirements

### Requirement: Entry option SPEED
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL request entry using SpeedPasses; the form without option keeps its behavior. Any option other than `SPEED` MUST be rejected with an error. `SPEED` MUST be rejected at `SPECIAL` attractions.

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error reporting an invalid entry option is printed and nobody enters

#### Scenario: SPEED at a special attraction
- **GIVEN** ticket `T001` valid today with an unconsumed Special Pass and SpeedPass
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed at special attraction `A04`
- **THEN** an error reporting that `A04` does not accept SpeedPasses is printed and no pass changes

### Requirement: Individual entry with SpeedPass
Individual SPEED entry at an attraction that accepts SpeedPasses SHALL require at least one unconsumed SpeedPass on the ticket; when entry succeeds exactly one available SpeedPass MUST be marked consumed and named in the output. If entry fails for any reason, no SpeedPass is modified.

#### Scenario: One SpeedPass consumed
- **GIVEN** ticket `T001` valid today with unconsumed SpeedPasses `SP01:1` and `SP01:2`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of the two passes is consumed and the output names it

#### Scenario: No SpeedPass available
- **GIVEN** ticket `T001` valid today whose SpeedPasses are all consumed (or none)
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error reporting the missing SpeedPass is printed and nobody enters

#### Scenario: Failed entry keeps passes
- **GIVEN** ticket `T001` with an unconsumed SpeedPass and a full attraction `A05`
- **WHEN** `ATTRACTION ENTER;A05;T001;SPEED` is processed
- **THEN** an error reporting insufficient capacity is printed and the SpeedPass is still unconsumed

### Requirement: Group entry with SpeedPasses
Group SPEED entry at `GENERAL` and `GROUP` attractions SHALL require, before any change, a valid group ticket, an attraction accepting group tickets, capacity for all members and an unconsumed SpeedPass on every constituent ticket; then all members enter and exactly one SpeedPass of each constituent ticket is consumed, as one indivisible operation. If any condition fails, no member enters and no SpeedPass is consumed.

#### Scenario: Whole group with SpeedPasses
- **GIVEN** group ticket `TG01` (Ana, Pedro) valid today with one unconsumed SpeedPass on each constituent
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both enter and both SpeedPasses are consumed

#### Scenario: One member without SpeedPass
- **GIVEN** group ticket `TG01` where only Ana's constituent has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error naming Pedro's constituent ticket is printed, nobody enters, and Ana's SpeedPass is still unconsumed

### Requirement: Special Pass consumed at special attractions
Entry to a `SPECIAL` attraction with the normal syntax SHALL automatically use the ticket's unconsumed Special Pass, mark it consumed only when entry succeeds, and report its consumption. Without a Special Pass, with a consumed one, or if any other condition fails, entry MUST be rejected without changing the pass.

#### Scenario: Special Pass consumed
- **GIVEN** ticket `T001` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, `PE01` is consumed and the output says so

#### Scenario: Already consumed
- **GIVEN** `PE01` was consumed by a previous entry
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed again after Ana exits
- **THEN** an error is printed and nobody enters

### Requirement: Constituent tickets usable for individual entry
An individual ticket that belongs to a group ticket's composition SHALL be usable on its own with `ATTRACTION ENTER` as an individual ticket (admitting only its user, with its own passes), so that personal passes bought for it can be used; the group ticket itself remains subject to each attraction type's restrictions.

#### Scenario: Special Pass on a constituent ticket
- **GIVEN** constituent ticket `TG01:12345678Z` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;TG01:12345678Z` is processed at special attraction `A04`
- **THEN** Ana enters and `PE01` is consumed, while `ATTRACTION ENTER;A04;TG01` is still rejected
