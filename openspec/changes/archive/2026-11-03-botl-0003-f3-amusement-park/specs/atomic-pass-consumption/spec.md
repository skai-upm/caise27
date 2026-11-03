# Spec Delta

## Purpose

Extends attraction entry with optional SpeedPass use and automatic Special Pass use, consuming passes only when the whole entry succeeds (source F3-C08, section 12).

## ADDED Requirements

### Requirement: Entry with SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL, at GENERAL, INDIVIDUAL and GROUP attractions accepting the ticket, require at least one unconsumed SpeedPass for every individual ticket that enters, and on success consume exactly one per individual ticket and report which passes were consumed; any other fourth parameter SHALL be rejected.

#### Scenario: Individual SpeedPass entry
- **WHEN** `T001` has unconsumed passes `SP01:1` and `SP02:1` and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and its identifier is shown

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and SPEED entry is attempted
- **THEN** entry is rejected and nothing changes

### Requirement: Atomic group SpeedPass entry
Group SPEED entry SHALL check validity for the current date, acceptance of group tickets, capacity for all members and an unconsumed SpeedPass for every composed ticket before changing state; if any check fails no member enters and no pass is consumed.

#### Scenario: One member lacks a SpeedPass
- **WHEN** a group ticket for 3 members has SpeedPasses for only 2 and SPEED entry is attempted
- **THEN** no member enters and no pass is consumed

#### Scenario: All members have SpeedPasses
- **WHEN** each composed ticket has an unconsumed SpeedPass and SPEED entry succeeds
- **THEN** all members enter and exactly one pass per composed ticket is consumed

### Requirement: Automatic Special Pass consumption
At a SPECIAL attraction, normal entry with an individual ticket SHALL require the ticket's unconsumed Special Pass and consume it only when entry succeeds, reporting the consumption; `SPEED` and group tickets SHALL be rejected there.

#### Scenario: Special entry consumes the pass
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` succeeds at SPECIAL `A04`
- **THEN** `PE01` is consumed and the output says so

#### Scenario: SPEED or missing pass at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed, or `T001` has no unconsumed Special Pass
- **THEN** entry is rejected and the Special Pass state is unchanged

### Requirement: Pass consumption persisted
A pass consumed by a successful entry SHALL remain consumed after a restart; if saving fails the entry SHALL be undone.

#### Scenario: Consumed state survives restart
- **WHEN** a SpeedPass is consumed and the application restarts
- **THEN** the pass is still stored and consumed
