# Spec Delta

## Purpose

Defines how passes are used when entering attractions: optional SpeedPass entry for individual and group tickets, automatic Special Pass use at special attractions, and the guarantee that no pass changes when entry fails.

## ADDED Requirements

### Requirement: SpeedPass entry with an individual ticket
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at a GENERAL or INDIVIDUAL attraction SHALL require at least one unconsumed SpeedPass on the ticket in addition to the normal entry conditions; on success exactly one of them SHALL be marked consumed and the output SHALL name it (`SPEEDPASS CONSUMED: <id> (ticket <ticket>)`). If entry fails for any reason no SpeedPass SHALL change.

#### Scenario: Consume one SpeedPass
- **WHEN** `T001` (valid today) has unconsumed SpeedPasses `SP01:1` and `SP01:2` and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, exactly one of the passes is consumed, and the output names it

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an `ERROR:` line is printed and nobody enters

#### Scenario: Failed entry keeps passes
- **WHEN** `T001` has an unconsumed SpeedPass but the attraction is full
- **THEN** an `ERROR:` line is printed and the SpeedPass is still unconsumed

### Requirement: Atomic group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at a GENERAL or GROUP attraction SHALL check, before changing anything, that the group ticket is valid today, the attraction accepts group tickets, there is room for all members and every composition ticket has an unconsumed SpeedPass. If all hold, all members SHALL enter and exactly one SpeedPass per composition ticket SHALL be consumed; otherwise nobody SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: One member without SpeedPass
- **WHEN** `TG01` has two members and only `TG01:12345678Z` has an unconsumed SpeedPass
- **THEN** `ATTRACTION ENTER;A03;TG01;SPEED` prints an `ERROR:` line, nobody enters and that SpeedPass stays unconsumed

#### Scenario: Whole group with SpeedPasses
- **WHEN** both composition tickets of `TG01` have an unconsumed SpeedPass
- **THEN** `ATTRACTION ENTER;A03;TG01;SPEED` lets both members in and consumes one SpeedPass of each

### Requirement: Special Pass use at special attractions
At a SPECIAL attraction, `ATTRACTION ENTER;<attraction>;<ticket>` SHALL automatically find the individual ticket's Special Pass, require that it is not consumed, and mark it consumed only when entry succeeds, reporting `SPECIALPASS CONSUMED: <id> (ticket <ticket>)`. The `SPEED` variant and group tickets SHALL be rejected at special attractions; on any failure the Special Pass SHALL NOT change.

#### Scenario: Special Pass consumed on entry
- **WHEN** `T001` (valid today) has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the user enters, `PE01` becomes consumed and the output reports it

#### Scenario: Already consumed
- **WHEN** `PE01` is consumed and the user (after leaving) tries `ATTRACTION ENTER;A04;T001` again
- **THEN** an `ERROR:` line is printed

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an `ERROR:` line is printed and no pass changes

### Requirement: ENTER option syntax
The optional fourth element of `ATTRACTION ENTER` SHALL be exactly `SPEED`; any other value SHALL be rejected with an `ERROR:` message.

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an `ERROR:` line is printed
