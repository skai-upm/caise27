# Spec Delta

## Purpose

Extends attraction entry with optional SpeedPass use and automatic Special Pass consumption, consuming passes only when entry succeeds.

## ADDED Requirements

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL, at GENERAL, INDIVIDUAL and GROUP attractions accepting the ticket, require an unconsumed SpeedPass for every entering individual ticket (the ticket itself or each composed ticket) and, on success, mark exactly one of them consumed per ticket and report which ones.

#### Scenario: Individual SpeedPass entry
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` succeeds for a ticket with unused SpeedPasses
- **THEN** exactly one SpeedPass is consumed and its identifier is shown

#### Scenario: Missing SpeedPass
- **WHEN** SPEED entry is attempted with a ticket without unused SpeedPasses
- **THEN** entry is rejected and no pass changes

#### Scenario: Invalid option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is executed
- **THEN** an error is reported

### Requirement: Atomic group SpeedPass entry
Group entry with SPEED SHALL check date validity, accepted kind, capacity for all members and an unconsumed SpeedPass for every composed ticket before changing state; if any check fails nobody enters and no pass is consumed.

#### Scenario: One member lacks a SpeedPass
- **WHEN** a group ticket enters with SPEED and one composed ticket has no unused SpeedPass
- **THEN** no member enters and no SpeedPass is consumed

#### Scenario: All members have SpeedPasses
- **WHEN** every composed ticket has an unused SpeedPass and there is room
- **THEN** all members enter and one SpeedPass per composed ticket is consumed

### Requirement: Special attraction entry
At SPECIAL attractions, normal entry SHALL require the individual ticket's unconsumed Special Pass and consume it only when entry succeeds, reporting the consumption; SPEED and group tickets SHALL be rejected.

#### Scenario: Special Pass consumed
- **WHEN** a valid individual ticket with an unused Special Pass enters a SPECIAL attraction
- **THEN** the user enters and the Special Pass is reported and marked consumed

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is executed
- **THEN** entry is rejected and no pass changes

#### Scenario: No or consumed Special Pass
- **WHEN** the ticket has no Special Pass or it is already consumed
- **THEN** entry is rejected without changing the pass

### Requirement: Failed entry changes nothing
If entry fails for any reason (date, kind, capacity, presence, missing pass), no user SHALL enter and no pass SHALL be modified.

#### Scenario: Full attraction with SpeedPass
- **WHEN** SPEED entry fails because the attraction is full
- **THEN** the SpeedPass remains unconsumed
