# atomic-pass-consumption Specification

## Purpose

Extends attraction entry with optional SpeedPass use and automatic Special Pass use, consuming passes only when entry succeeds and atomically for groups.

## Requirements

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL be accepted at GENERAL, INDIVIDUAL and GROUP attractions for the ticket types they accept, after all previous entry checks. Each individual ticket entering (the ticket itself, or every composition ticket of a group ticket) SHALL have at least one unconsumed SpeedPass; on success exactly one SpeedPass per entering individual ticket SHALL be marked consumed and the output SHALL name the consumed passes. Any other fourth element SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has unused SpeedPass SP01:1 and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** the user enters, SP01:1 is consumed and the output names SP01:1

#### Scenario: No unused SpeedPass
- **WHEN** the ticket has no unconsumed SpeedPass
- **THEN** entry is rejected and nobody enters

### Requirement: Atomic group SpeedPass entry
For a group ticket with `SPEED`, validity for the current date, acceptance by the attraction type, capacity for all members and an unconsumed SpeedPass for every composition ticket SHALL all be checked before any change; if any fails, nobody SHALL enter and no pass SHALL be consumed.

#### Scenario: One member lacks a SpeedPass
- **WHEN** one composition ticket of TG01 has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** no member enters and no SpeedPass is consumed

#### Scenario: All members have SpeedPasses
- **WHEN** every composition ticket of TG01 has an unconsumed SpeedPass and capacity suffices
- **THEN** all members enter and exactly one SpeedPass per composition ticket is consumed

### Requirement: Automatic Special Pass consumption
At a SPECIAL attraction, `ATTRACTION ENTER;<attraction>;<ticket>` SHALL find the ticket's Special Pass automatically, require it to be unconsumed, mark it consumed only when entry succeeds and state so in the output. The `SPEED` variant SHALL be rejected at SPECIAL attractions. A missing or consumed Special Pass, or any other failed condition, SHALL leave the pass unchanged.

#### Scenario: Special entry
- **WHEN** T001 has unused Special Pass PE01 and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction A04
- **THEN** the user enters, PE01 is consumed and the output says so

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** entry is rejected and no pass changes

#### Scenario: Consumed Special Pass
- **WHEN** T001's Special Pass is already consumed
- **THEN** entry is rejected
