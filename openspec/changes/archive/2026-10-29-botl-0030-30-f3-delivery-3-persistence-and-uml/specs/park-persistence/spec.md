# Spec Delta

## ADDED Requirements

### Requirement: Passes are persisted
SpeedPasses and Special Passes SHALL be stored with their identifier, kind, associated individual ticket, stored price and consumed state, and SHALL be reconstructed on startup linked to the same individual ticket (standalone or part of a group ticket), with the same price and state. Consumed passes SHALL remain stored while their ticket exists; passes deleted by refunds or by ticket deletion SHALL disappear from the stored data.

#### Scenario: Passes survive a restart
- **WHEN** `T001` has SpeedPasses `SP01:1` (consumed) and `SP01:2`, and Special Pass `PE01`, and the application is started again
- **THEN** the three passes exist with the same prices, `SP01:1` is consumed and the other two are not, and a second Special Pass for `T001` is still rejected

#### Scenario: Consumption is saved
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` consumes `SP01:2` and the application is started again
- **THEN** `SP01:2` is consumed and `SPEEDPASS DELETE;SP01:2` is rejected

#### Scenario: Cascade reflected in the files
- **WHEN** a future ticket with passes is deleted with `TICKET DELETE`
- **THEN** after restart neither the ticket nor its passes exist

### Requirement: Temporary mode includes passes
In `--fly` mode the passes file SHALL be created only inside the temporary directory, using the same storage classes as normal mode.

#### Scenario: Fly mode passes
- **WHEN** passes are bought in a `--fly` execution
- **THEN** the permanent `passes.txt` is not created or changed
