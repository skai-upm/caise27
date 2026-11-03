# Spec Delta

## Purpose

Uses SpeedPasses and Special Passes when entering attractions, consuming them only when the whole entry succeeds.

## ADDED Requirements

### Requirement: Normal entry preserved
`ATTRACTION ENTER;<atraccion>;<entrada>` without option SHALL behave as before at GENERAL, INDIVIDUAL and GROUP attractions and SHALL NOT consume any pass.

#### Scenario: Normal entry
- **GIVEN** `T001` valid today with SpeedPasses and a GENERAL attraction `A01`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana enters and no pass is consumed

### Requirement: Individual entry with SPEED
`ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` with an individual ticket at a GENERAL or INDIVIDUAL attraction SHALL require at least one unused SpeedPass of that ticket and, when the entry succeeds, SHALL consume exactly one (the earliest bought) and name it in the response.

#### Scenario: One of several SpeedPasses consumed
- **GIVEN** `T001` has unused SpeedPasses `S1-1` and `S1-2`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** the response names `S1-1`, `S1-1` is consumed and `S1-2` is not

#### Scenario: No SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T002;SPEED` is processed for a ticket without unused SpeedPasses
- **THEN** an error is reported and nobody enters

### Requirement: Group entry with SPEED
With a group ticket at a GENERAL or GROUP attraction, `SPEED` SHALL require an unused SpeedPass on every constituent ticket; on success all members SHALL enter and one SpeedPass per constituent ticket SHALL be consumed; if any member lacks one or any other access condition fails, no member SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: Member without SpeedPass
- **GIVEN** `TG01` where only `TG01-1` has a SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is reported, nobody enters and the SpeedPass of `TG01-1` is still unused

#### Scenario: Capacity failure
- **GIVEN** every constituent ticket of `TG01` has a SpeedPass but the attraction has fewer free places than members
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error is reported, nobody enters and no SpeedPass is consumed

#### Scenario: Successful group SPEED entry
- **GIVEN** every constituent ticket of `TG01` has an unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** all members enter and exactly one SpeedPass per member is consumed and named

### Requirement: Special attraction entry
At SPECIAL attractions an individual ticket SHALL enter only if it has an unconsumed Special Pass, which SHALL be consumed automatically when the entry succeeds and named in the response; the `SPEED` option SHALL be rejected without changing any pass.

#### Scenario: Special Pass consumed
- **GIVEN** `T001` has unused Special Pass `SP1` and SPECIAL attraction `A04`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, `SP1` is consumed and the response names it

#### Scenario: Missing or used Special Pass
- **WHEN** a ticket without Special Pass, or whose Special Pass is consumed, tries to enter `A04`
- **THEN** an error is reported and nobody enters

#### Scenario: SPEED at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is reported and no pass changes

### Requirement: Failed entries consume nothing
Any rejected entry (wrong ticket kind, wrong date, already present, capacity, missing pass, invalid option) SHALL leave the attraction occupancy and every pass status unchanged; consumed statuses of successful entries SHALL be persisted.

#### Scenario: Invalid option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error is reported and nothing changes

#### Scenario: Consumption survives restart
- **GIVEN** `S1-1` was consumed by a SPEED entry
- **WHEN** the application restarts
- **THEN** `S1-1` is consumed and cannot be refunded
