# Spec Delta

## ADDED Requirements

### Requirement: Passes persisted
SpeedPasses and Special Passes SHALL be stored with their type, identifier, associated individual ticket, stored price and consumed state, and SHALL be reconstructed linked to the same individual tickets when the application starts again. Consumed passes SHALL remain stored while their ticket exists; passes removed by refunds or by valid ticket deletions SHALL no longer be stored.

#### Scenario: Passes survive a restart
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` and `SPECIALPASS CREATE;PE01;T001` are processed, the application ends and starts again
- **THEN** `SP02:1` … `SP02:10` and `PE01` exist for `T001` with prices `8.00` and `40.00` and unconsumed

#### Scenario: Consumed state survives a restart
- **WHEN** `PE01` was consumed by entering a SPECIAL attraction, and the application restarts
- **THEN** `PE01` is still stored for `T001` and is consumed, so `T001` cannot enter the SPECIAL attraction again nor buy another Special Pass

#### Scenario: Stored price independent of the current price
- **WHEN** a pass bought at `8.00` is reloaded after a restart in which `SYSTEM PRICE;100.00` is processed
- **THEN** the pass still costs `8.00`

#### Scenario: Refunded and cascaded passes not restored
- **WHEN** `SP02:3` was refunded and future ticket `T002` with passes was deleted, and the application restarts
- **THEN** `SP02:3` and the passes of `T002` do not exist

#### Scenario: Temporary mode
- **WHEN** passes are bought in a `--fly` execution
- **THEN** they are stored only inside the temporary directory, which is deleted at the end of the execution
