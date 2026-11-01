# Spec Delta

## Purpose

Extends attraction entry so visitors can use SpeedPasses and Special Passes, consuming them only when an entry fully succeeds.

## ADDED Requirements

### Requirement: SPEED entry option
`ATTRACTION ENTER;<AtraccionId>;<TicketId>;SPEED` SHALL request SpeedPass entry; `ATTRACTION ENTER;<AtraccionId>;<TicketId>` SHALL keep normal entry without consuming passes. Any fourth parameter other than `SPEED` SHALL be rejected.

#### Scenario: Normal entry consumes nothing
- **GIVEN** a valid ticket with unused SpeedPasses at a `GENERAL` attraction
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** the user enters and all SpeedPasses stay unused

### Requirement: Individual SpeedPass entry
At `GENERAL` and `INDIVIDUAL` attractions an individual ticket entering with `SPEED` SHALL consume exactly one unused SpeedPass of that ticket when, and only when, the entry succeeds, and the output SHALL report the consumed pass identifier. Without an unused SpeedPass the entry SHALL be rejected.

#### Scenario: One pass consumed
- **GIVEN** ticket `T001` valid today with unused SpeedPasses `T001-SP1` and `T001-SP2`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, exactly one SpeedPass becomes consumed, and its identifier is reported

#### Scenario: Failed entry consumes nothing
- **GIVEN** a full attraction and ticket `T001` with an unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is reported and the SpeedPass stays unused

### Requirement: Atomic group SpeedPass entry
At `GENERAL` and `GROUP` attractions a group ticket entering with `SPEED` SHALL require every constituent ticket to have at least one unused SpeedPass and every ordinary entry condition to hold before changing anything; on success all members enter and one SpeedPass per constituent ticket is consumed and reported; on failure nobody enters and no pass is consumed.

#### Scenario: One member lacks a SpeedPass
- **GIVEN** group ticket `TG01` where `TG01-1` has an unused SpeedPass and `TG01-2` has none
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error message is reported, no member enters, and `TG01-1`'s SpeedPass stays unused

#### Scenario: Whole group with passes
- **GIVEN** group ticket `TG01` whose two constituent tickets each have an unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** both members enter and one SpeedPass of each constituent ticket is consumed

### Requirement: Special Pass entry
At `SPECIAL` attractions an individual ticket SHALL enter only if it has an unused Special Pass, which SHALL be consumed automatically when the entry succeeds and reported in the output. The `SPEED` option SHALL be rejected at `SPECIAL` attractions.

#### Scenario: Special Pass consumed automatically
- **GIVEN** a `SPECIAL` attraction and ticket `T001` valid today with an unused Special Pass
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** the user enters and the output reports that `T001-SPECIAL` was consumed

#### Scenario: SPEED rejected at SPECIAL
- **WHEN** `ATTRACTION ENTER;A02;T001;SPEED` is processed at a `SPECIAL` attraction
- **THEN** an error message is reported and no pass is consumed

#### Scenario: Missing or consumed Special Pass
- **GIVEN** ticket `T001` without an unused Special Pass
- **WHEN** it is used to enter a `SPECIAL` attraction
- **THEN** an error message is reported and nobody enters

### Requirement: Pass consumption is persisted
Every pass consumed by a successful entry SHALL be persisted as consumed immediately.

#### Scenario: Consumption survives restart
- **GIVEN** a SpeedPass consumed by an entry
- **WHEN** the application restarts
- **THEN** that SpeedPass is still consumed
