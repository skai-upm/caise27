# Spec Delta

## ADDED Requirements

### Requirement: Passes persisted with their ticket, price and state
SpeedPasses and Special Passes SHALL be stored with their type, identifier, owning individual ticket (bought directly or as a group-ticket constituent), stored price and consumed state, and reconstructed at start-up linked to that ticket. Consumed passes MUST remain stored while their ticket exists; refunded passes and the passes of refunded tickets MUST no longer be stored. Passes whose ticket no longer exists MUST be dropped when loading.

#### Scenario: Passes recovered after restart
- **GIVEN** ticket `T001` with SpeedPasses `SP01:1` (consumed) and `SP01:2`, constituent `TG01:12345678Z` with Special Pass `PE01`, all created in a previous run
- **WHEN** the application starts again
- **THEN** the three passes exist with the same tickets and prices, `SP01:1` is consumed, `SP01:2` and `PE01` are not, and `SPECIALPASS CREATE;PE02;TG01:12345678Z` is rejected because the ticket already has a Special Pass

#### Scenario: Consumption persisted
- **GIVEN** `PE01` is consumed by entering a special attraction
- **WHEN** the application restarts
- **THEN** `PE01` is still consumed and cannot be used again

#### Scenario: Refunds removed from storage
- **WHEN** `SPEEDPASS DELETE;SP01:2` and later `TICKET DELETE` of a future ticket with passes are processed and the application restarts
- **THEN** neither the refunded pass nor the deleted ticket's passes exist

#### Scenario: Fly mode covers passes
- **WHEN** passes are bought during a `--fly` execution
- **THEN** they are stored only inside the temporary directory and the permanent pass file is unchanged
