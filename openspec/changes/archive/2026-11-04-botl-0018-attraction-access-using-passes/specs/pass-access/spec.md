# Spec Delta

## Purpose

Lets visitors use their SpeedPasses and Special Passes when entering attractions, consuming them only when the whole entry succeeds.

## ADDED Requirements

### Requirement: Entry options
`ATTRACTION ENTER;<atraccion>;<entrada>` SHALL keep the normal entry behaviour, and `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` SHALL request the use of SpeedPasses; any other third parameter SHALL be rejected with `ERROR:`.

#### Scenario: Normal entry unchanged
- **GIVEN** GENERAL attraction `A01` and ticket `T001` valid today with unused SpeedPasses
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana enters and no pass is consumed

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an `ERROR:` response is printed and nobody enters

### Requirement: Individual entry with SPEED
At GENERAL and INDIVIDUAL attractions, an individual ticket entering with `SPEED` SHALL require at least one unconsumed SpeedPass and SHALL consume exactly one (the earliest purchased unused one) only when the entry succeeds; the response SHALL identify the consumed pass.

#### Scenario: One of several passes consumed
- **GIVEN** `T001` has unused SpeedPasses `P02-1` … `P02-10`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the response is `OK: 1 user(s) entered attraction A01; SpeedPass consumed: P02-1`, `P02-1` is consumed and `P02-2` … `P02-10` are not

#### Scenario: No SpeedPass
- **GIVEN** `T001` has no unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an `ERROR:` response is printed and nobody enters

### Requirement: Group entry with SPEED is atomic
At GENERAL and GROUP attractions, a group ticket entering with `SPEED` SHALL require an unconsumed SpeedPass for every constituent ticket and SHALL consume exactly one per constituent; if any constituent lacks one or any other access condition fails, no member SHALL enter and no pass SHALL be consumed.

#### Scenario: All members have passes
- **GIVEN** each of the 3 constituent tickets of `TG01` has an unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** the 3 members enter, one SpeedPass per constituent is consumed, and the response lists the 3 consumed passes

#### Scenario: A member without pass
- **GIVEN** one constituent of `TG01` has no unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** an `ERROR:` response is printed, nobody enters and no pass is consumed

#### Scenario: Not enough capacity
- **GIVEN** every constituent has a SpeedPass but the attraction has fewer free places than members
- **WHEN** the group enters with `SPEED`
- **THEN** nobody enters and no pass is consumed

### Requirement: Special Pass at SPECIAL attractions
SPECIAL attractions SHALL admit individual tickets only, SHALL locate the ticket's Special Pass automatically, SHALL reject the entry if it has none or it is consumed, and SHALL consume it only after successful entry, reporting it in the response; the `SPEED` option SHALL be rejected there without changing any pass.

#### Scenario: Special entry
- **GIVEN** SPECIAL attraction `A04` and ticket `T001` valid today with unused Special Pass `S01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the response is `OK: 1 user(s) entered attraction A04; Special Pass consumed: S01` and `S01` is consumed

#### Scenario: Special Pass missing or used
- **WHEN** a ticket without Special Pass, or whose Special Pass is consumed, enters `A04`
- **THEN** an `ERROR:` response is printed and nobody enters

#### Scenario: SPEED rejected at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an `ERROR:` response is printed, nobody enters and no SpeedPass or Special Pass changes state

### Requirement: Failed entries consume nothing and consumption persists
Any rejected entry SHALL leave every pass unchanged; consumed passes SHALL be saved as consumed, survive restarts and SHALL NOT be refundable.

#### Scenario: Consumption survives restart
- **GIVEN** `P02-1` was consumed by a SPEED entry and the application restarted
- **WHEN** `SPEEDPASS DELETE;P02-1` is processed
- **THEN** an `ERROR:` response is printed and `P02-1` is still recorded as consumed
