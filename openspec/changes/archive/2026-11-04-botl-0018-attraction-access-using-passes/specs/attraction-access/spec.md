# Spec Delta

## MODIFIED Requirements

### Requirement: Entry with a ticket
`ATTRACTION ENTER;IdAtraccion;IdTicket` (and, with SpeedPass use, `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED`) SHALL admit the users represented by the ticket: the owner for an individual ticket, or the owners of all constituent tickets for a group ticket. Individual and group tickets SHALL be handled by the same entry operation. Unknown attractions or tickets SHALL be rejected.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15, attraction `A01` with free places and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an `OK` message is produced and Ana is present in `A01`

#### Scenario: Group entry
- **GIVEN** group ticket `TG01` dated 2026-10-15 composed of the tickets of Pedro and Luis
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` or `ATTRACTION ENTER;A99;T001` is processed
- **THEN** an `ERROR` message is produced and nobody enters

#### Scenario: Normal entry unchanged
- **GIVEN** a GENERAL attraction and an individual ticket valid today with unused SpeedPasses
- **WHEN** `ATTRACTION ENTER;A01;T001` (without `SPEED`) is processed
- **THEN** the owner enters and no pass is consumed

## ADDED Requirements

### Requirement: SpeedPass entry
With the `SPEED` option, at `GENERAL`, `INDIVIDUAL` and `GROUP` attractions (subject to their accepted ticket kinds), every individual ticket being used — the ticket itself, or each constituent ticket of a group ticket — SHALL have at least one unconsumed SpeedPass. On successful entry exactly one unconsumed SpeedPass per such ticket SHALL be marked consumed, and the reply SHALL name the consumed passes. Any other value than `SPEED` in the fourth position SHALL be rejected.

#### Scenario: Individual entry with SPEED
- **GIVEN** ticket `T001` valid today with unconsumed SpeedPasses `SP1` and `SP2`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed at a GENERAL attraction
- **THEN** the owner enters, exactly one of the passes (`SP1`) becomes consumed, and the reply names `SP1`

#### Scenario: Group entry with SPEED
- **GIVEN** group ticket `TG01` whose constituents `TG01-1` and `TG01-2` each have an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed at a GROUP attraction
- **THEN** both members enter and one SpeedPass of each constituent is consumed

#### Scenario: Missing SpeedPass
- **GIVEN** `TG01-2` has no unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** an `ERROR` message is produced, no member enters and no SpeedPass is consumed

#### Scenario: Invalid option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an `ERROR` message is produced and nothing changes

### Requirement: SPECIAL attraction entry
At `SPECIAL` attractions only individual tickets SHALL be admitted, the `SPEED` option SHALL be rejected, and the ticket SHALL have an unconsumed Special Pass, which SHALL be located automatically and marked consumed only after successful entry; the reply SHALL name the consumed Special Pass.

#### Scenario: Special Pass consumed on entry
- **GIVEN** SPECIAL attraction `A04` and ticket `T001` valid today with unconsumed Special Pass `SX1`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the owner enters, `SX1` becomes consumed and the reply names `SX1`

#### Scenario: Special Pass missing or already consumed
- **WHEN** a ticket without a Special Pass, or whose Special Pass is consumed, enters `A04`
- **THEN** an `ERROR` message is produced and nobody enters

#### Scenario: SPEED rejected at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an `ERROR` message is produced, nobody enters and no pass is consumed

### Requirement: Failed entries consume nothing
Pass availability SHALL be checked before admission and passes SHALL be marked consumed only after every access condition (ticket kind, date, holders present, not already inside, capacity) has passed and the users were admitted; if any condition fails no user SHALL enter and no pass SHALL be consumed. Consumed states SHALL be persisted.

#### Scenario: Capacity failure keeps passes
- **GIVEN** a group with SpeedPasses for every constituent and an attraction without enough room
- **WHEN** `ATTRACTION ENTER;A02;TG01;SPEED` is processed
- **THEN** an `ERROR` message is produced and every SpeedPass is still unconsumed

#### Scenario: Wrong date keeps the Special Pass
- **WHEN** a ticket dated tomorrow with an unconsumed Special Pass tries to enter a SPECIAL attraction
- **THEN** an `ERROR` message is produced and the Special Pass is still unconsumed

#### Scenario: Consumption persisted
- **GIVEN** `SP1` was consumed by an entry
- **WHEN** the application restarts
- **THEN** `SP1` is still consumed
