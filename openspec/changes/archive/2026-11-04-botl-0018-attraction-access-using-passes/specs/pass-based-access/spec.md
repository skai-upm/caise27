# Spec Delta

## Purpose

Extends attraction entry so that SpeedPasses can be used on request and Special Passes are required at SPECIAL attractions, consuming passes only when the whole entry succeeds.

## ADDED Requirements

### Requirement: Entry command forms
The system SHALL accept `ATTRACTION ENTER;IdAtraccion;IdTicket` (normal entry, unchanged) and `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED` (entry using SpeedPasses). Any other third parameter, or a different parameter count, SHALL be rejected with `ERROR` without admitting anyone or consuming any pass.

#### Scenario: Normal entry unchanged
- **GIVEN** `T001` is valid for today at the GENERAL attraction `A01` and has unused SpeedPasses
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana enters, no pass is consumed, and the output is the same as before this change

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** the result starts with `ERROR`, nobody enters and no pass changes

### Requirement: Individual entry with SPEED
At GENERAL and INDIVIDUAL attractions, an individual ticket entering with `SPEED` SHALL be admitted only if all normal entry conditions hold and the ticket has at least one unused SpeedPass; on success exactly one of its unused SpeedPasses SHALL be marked consumed and identified in the output.

#### Scenario: One SpeedPass consumed
- **GIVEN** `T001` has unused SpeedPasses `SP1-1` to `SP1-10`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them (`SP1-1`) is consumed, the output names it, and nine remain unused, also after a restart

#### Scenario: No SpeedPass
- **GIVEN** `T002` has no unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T002;SPEED` is processed
- **THEN** the result starts with `ERROR` and the visitor does not enter

### Requirement: Group entry with SPEED
At GENERAL and GROUP attractions, a group ticket entering with `SPEED` SHALL be admitted only if all normal entry conditions hold and every individual ticket of its composition has at least one unused SpeedPass. On success one SpeedPass per member ticket SHALL be consumed and identified in the output. If any member ticket lacks an unused SpeedPass or any other condition fails, no visitor SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: Member without SpeedPass
- **GIVEN** `TG01` has three member tickets and only two of them have unused SpeedPasses
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** the result starts with `ERROR`, nobody enters and no pass is consumed

#### Scenario: Not enough capacity
- **GIVEN** every member ticket of `TG01` has an unused SpeedPass but the attraction has fewer free places than members
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** the result starts with `ERROR`, nobody enters and no pass is consumed

#### Scenario: Successful group SPEED entry
- **GIVEN** every member ticket of `TG01` has an unused SpeedPass and there is room
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** all members enter and exactly one SpeedPass per member ticket is consumed and listed in the output

### Requirement: SPECIAL attraction entry
At SPECIAL attractions only individual tickets SHALL enter. The system SHALL locate the ticket's Special Pass automatically and admit the visitor only if it exists, is unused and all normal entry conditions hold; on success the Special Pass SHALL be marked consumed and identified in the output. The `SPEED` option SHALL be rejected at SPECIAL attractions without admitting anyone or changing any pass.

#### Scenario: Special Pass consumed
- **GIVEN** `T001` has the unused Special Pass `VIP1`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed at the SPECIAL attraction `A04`
- **THEN** Ana enters, `VIP1` becomes consumed and the output names it

#### Scenario: Missing or used Special Pass
- **WHEN** `ATTRACTION ENTER;A04;T002` is processed for a ticket without a Special Pass, or for one whose Special Pass is already consumed
- **THEN** the result starts with `ERROR` and nobody enters

#### Scenario: SPEED at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** the result starts with `ERROR`, nobody enters and no pass changes

### Requirement: Failed entries consume nothing
Pass consumption SHALL happen only after every entry condition has been checked, and an entry that fails for any reason (type, date, capacity, visitor already inside, missing pass, storage failure) SHALL leave both the attraction occupancy and all pass states unchanged.

#### Scenario: Wrong date with SPEED
- **WHEN** a ticket for another day requests `SPEED` entry
- **THEN** the result starts with `ERROR` and its SpeedPasses stay unused
