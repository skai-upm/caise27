# Spec Delta

## Purpose

Defines SpeedPass use with `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` and automatic Special Pass consumption, with all-or-nothing behaviour (F3-C08).

## ADDED Requirements

### Requirement: SpeedPass entry
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL be accepted at GENERAL, INDIVIDUAL and GROUP attractions for ticket kinds the attraction accepts. In addition to the Delivery 2 checks, every entering individual ticket SHALL have at least one unconsumed SpeedPass; on success exactly one SpeedPass per entrant SHALL be marked consumed and the output SHALL name the consumed passes. Any other fourth parameter SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and its identifier is shown

#### Scenario: No unused SpeedPass
- **WHEN** a SPEED entry is attempted with a ticket that has no unconsumed SpeedPass
- **THEN** entry is rejected and no pass changes

### Requirement: Atomic group SpeedPass entry
For a group ticket with `SPEED`, validity for today, acceptance of group tickets, capacity for all members and an unconsumed SpeedPass for every constituent SHALL all be checked before changing state; if any fails, no member SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: One member lacks a SpeedPass
- **WHEN** a group ticket enters with `SPEED` and one constituent has no unconsumed SpeedPass
- **THEN** no member enters and no SpeedPass is consumed

#### Scenario: All members have SpeedPasses
- **WHEN** every constituent has an unconsumed SpeedPass and capacity suffices
- **THEN** all members enter and exactly one SpeedPass per constituent is consumed

### Requirement: Special Pass consumption
At a SPECIAL attraction, normal-syntax entry SHALL automatically use the ticket's Special Pass, which SHALL exist and be unconsumed; it SHALL be marked consumed only when entry succeeds and the output SHALL say so. The `SPEED` variant SHALL be rejected at SPECIAL attractions.

#### Scenario: Special entry consumes the pass
- **WHEN** T001 has an unconsumed Special Pass and `ATTRACTION ENTER;A04;T001` succeeds at SPECIAL attraction A04
- **THEN** the Special Pass is consumed and the output says so

#### Scenario: Missing or consumed Special Pass
- **WHEN** the ticket has no Special Pass or it is already consumed
- **THEN** entry is rejected and the pass state is unchanged

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** entry is rejected and no pass changes
