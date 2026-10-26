# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL attractions only individual tickets (standalone or internal to a group ticket); GROUP attractions only group tickets; SPECIAL attractions only individual tickets whose Special Pass is AVAILABLE. At a SPECIAL attraction the Special Pass SHALL be located automatically and SHALL become CONSUMED only after a successful entry; the `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special attraction with Special Pass
- **WHEN** `A04` is SPECIAL, ticket `T001` for today has Special Pass `T001-SPECIAL` AVAILABLE and `ATTRACTION ENTER;A04;T001` is received
- **THEN** Ana enters `A04`, `T001-SPECIAL` becomes CONSUMED and the response reports that `T001-SPECIAL` was consumed

#### Scenario: Special attraction without usable Special Pass
- **WHEN** `T002` has no Special Pass, or `T001-SPECIAL` is already CONSUMED, and the ticket is used to enter `A04`
- **THEN** an error is printed and nobody enters

#### Scenario: Group ticket at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;TG01` is received
- **THEN** an error indicating the attraction does not admit that ticket kind is printed

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is received
- **THEN** an error is printed, nobody enters and no pass is consumed

#### Scenario: Special Pass kept when entry fails
- **WHEN** `A04` is full and `ATTRACTION ENTER;A04;T001` is received
- **THEN** an error is printed and `T001-SPECIAL` stays AVAILABLE

## ADDED Requirements

### Requirement: Entry with SpeedPasses
The command `ATTRACTION ENTER;Id;IdTicket;SPEED` SHALL perform the normal entry and additionally consume one AVAILABLE SpeedPass per admitted individual ticket: the ticket itself for an individual ticket (at GENERAL and INDIVIDUAL attractions) or each internal ticket of a group ticket (at GENERAL and GROUP attractions). If any of those tickets has no AVAILABLE SpeedPass, or any other entry condition fails, nobody SHALL enter and no pass SHALL be consumed. Any fourth parameter other than `SPEED` SHALL be rejected with an error.

#### Scenario: Individual SPEED entry
- **WHEN** `T001` (for today) has AVAILABLE SpeedPasses `T001-S1` and `T001-S2` and `ATTRACTION ENTER;A01;T001;SPEED` is received
- **THEN** Ana enters, exactly `T001-S1` becomes CONSUMED and the response reports it

#### Scenario: Group SPEED entry
- **WHEN** group ticket `TG01` has internal tickets `TG01-1` and `TG01-2`, each with an AVAILABLE SpeedPass, and `ATTRACTION ENTER;A01;TG01;SPEED` is received
- **THEN** both members enter and one SpeedPass of each internal ticket becomes CONSUMED

#### Scenario: Group SPEED entry is atomic
- **WHEN** `TG01-2` has no AVAILABLE SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is received
- **THEN** an error is printed, nobody enters and the SpeedPass of `TG01-1` stays AVAILABLE

#### Scenario: Normal entry consumes nothing
- **WHEN** `ATTRACTION ENTER;A01;T001` is received for a ticket with SpeedPasses
- **THEN** Ana enters and all her SpeedPasses stay AVAILABLE

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is received
- **THEN** an error is printed and nobody enters

### Requirement: Pass consumption recorded
Passes consumed by an attraction entry SHALL be reported in the response and their CONSUMED status SHALL be persisted, so it is kept after a restart.

#### Scenario: Consumption survives restart
- **WHEN** `T001-S1` is consumed by a SPEED entry and the application is restarted
- **THEN** `T001-S1` is CONSUMED and cannot be refunded
