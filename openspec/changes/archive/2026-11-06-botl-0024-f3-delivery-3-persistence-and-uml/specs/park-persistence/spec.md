# Spec Delta

## ADDED Requirements

### Requirement: Stored passes follow ticket lifecycle
Consumed passes SHALL remain in stored data while their individual ticket exists. When a ticket or group ticket is validly deleted, the passes of the deleted individual tickets SHALL also disappear from stored data. Refunded passes SHALL disappear from stored data.

#### Scenario: Consumed pass kept, cascade applied
- **GIVEN** ticket `T001` (today) with a consumed SpeedPass, and future ticket `T002` with passes
- **WHEN** `T002` is deleted and the application restarts
- **THEN** the consumed SpeedPass of `T001` is still stored and consumed
- **AND** no pass of `T002` is stored

### Requirement: Temporary mode covers Delivery 3 elements
In `--fly` mode, typed attractions and passes SHALL be stored through the same repositories as in normal mode, configured with the temporary directory, and SHALL be removed with that directory at the end of the execution.

#### Scenario: Fly run with passes
- **WHEN** a `--fly` run creates a SPECIAL attraction, a ticket and passes, and enters with SPEED
- **THEN** the commands succeed during the run
- **AND** afterwards no temporary directory remains and normal data is unchanged
