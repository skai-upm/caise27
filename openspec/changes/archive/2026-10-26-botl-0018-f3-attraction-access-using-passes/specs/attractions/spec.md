# Spec Delta

## MODIFIED Requirements

### Requirement: Accepted ticket kinds per type
`GENERAL` attractions SHALL admit individual and group tickets; `INDIVIDUAL` attractions SHALL admit only individual tickets; `GROUP` attractions SHALL admit only group tickets; `SPECIAL` attractions SHALL admit only individual tickets as the basis for access, subject to the Special Pass rules. Entry with a ticket kind the type does not admit SHALL be rejected without anyone entering.

#### Scenario: GROUP rejects an individual ticket
- **WHEN** `A02` is `GROUP` and `ATTRACTION ENTER;A02;T001` is processed for an individual ticket
- **THEN** an error is reported and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **WHEN** `A03` is `INDIVIDUAL` and `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: SPECIAL uses an individual ticket
- **WHEN** `A04` is `SPECIAL` and a valid individual ticket with an unused Special Pass is used
- **THEN** its user is admitted, while a group ticket is rejected

## ADDED Requirements

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` SHALL request SpeedPass use. At GENERAL and INDIVIDUAL attractions an individual ticket SHALL consume exactly one of its unused SpeedPasses. At GENERAL and GROUP attractions a group ticket SHALL require at least one unused SpeedPass on every individual ticket of its composition and SHALL consume one per member. Without SPEED, entry SHALL work as before and consume no pass.

#### Scenario: Individual SPEED entry
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the response names it

#### Scenario: Group SPEED entry
- **WHEN** every ticket of TG01 has an unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` succeeds
- **THEN** one SpeedPass per member is consumed and all members are inside

#### Scenario: Member without SpeedPass
- **WHEN** one ticket of TG01 has no unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** the entry is rejected, nobody enters and no SpeedPass is consumed

#### Scenario: Normal entry keeps passes
- **WHEN** `ATTRACTION ENTER;A01;T001` succeeds for a ticket with SpeedPasses
- **THEN** no pass is consumed

### Requirement: Entry at SPECIAL attractions
At SPECIAL attractions the system SHALL locate the Special Pass of the individual ticket, reject the entry if it is missing or already consumed, and mark it consumed only after the entry succeeds. The `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;A04;T001` succeeds at SPECIAL attraction `A04`
- **THEN** Ana enters and the response reports that the Special Pass was consumed

#### Scenario: Used or missing Special Pass
- **WHEN** the Special Pass is already consumed or does not exist
- **THEN** the entry is rejected

#### Scenario: SPEED at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** the entry is rejected and no pass is consumed

### Requirement: Failed entries consume nothing
Any rejected entry (wrong ticket kind or date, capacity, user already inside, missing or used passes) SHALL leave every pass unconsumed and nobody inside.

#### Scenario: Full attraction with SPEED
- **WHEN** an attraction is full and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the entry is rejected and T001's SpeedPasses stay unused
