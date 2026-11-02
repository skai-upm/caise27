# atomic-pass-consumption Specification

## Purpose

Extends attraction entry with optional SpeedPass use and automatic Special Pass use, consuming passes only when the whole entry succeeds.

## Requirements

### Requirement: SpeedPass entry
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL, at GENERAL, INDIVIDUAL and GROUP attractions accepting the ticket, require at least one unconsumed SpeedPass for every entering individual ticket (the ticket itself, or each constituent of a group ticket) in addition to all earlier entry checks; on success exactly one SpeedPass per entering ticket SHALL be marked consumed and the output SHALL name the consumed passes. (F3-C08)

#### Scenario: Individual SPEED entry
- **WHEN** T001 has unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** the user enters and exactly one of T001's SpeedPasses, named in the output, becomes consumed

#### Scenario: Group member without SpeedPass
- **WHEN** one constituent of a group ticket lacks an unconsumed SpeedPass
- **THEN** no member enters and no SpeedPass is consumed

#### Scenario: Any other failure
- **WHEN** a SPEED entry fails for capacity, date or ticket kind
- **THEN** no SpeedPass is modified

### Requirement: Special attraction entry
At a SPECIAL attraction, `ATTRACTION ENTER;<attraction>;<ticket>` SHALL require the individual ticket's Special Pass to exist and be unconsumed, SHALL mark it consumed only when entry succeeds and SHALL say so in the output; the SPEED variant SHALL be rejected. (F3-C08)

#### Scenario: Successful special entry
- **WHEN** T001 has an unused Special Pass and enters special attraction A04
- **THEN** the user is inside A04 and the Special Pass is consumed

#### Scenario: Missing or used pass, or SPEED
- **WHEN** T001 has no Special Pass, its pass is consumed, or `;SPEED` is given at A04
- **THEN** entry is rejected and the pass state is unchanged

### Requirement: Persisted consumption
Pass consumption SHALL be saved with the entry; if saving fails, neither occupancy nor any pass state SHALL change. (F3-C08)

#### Scenario: Consumption survives restart
- **WHEN** a pass is consumed on entry and the application restarts
- **THEN** the pass is still consumed
