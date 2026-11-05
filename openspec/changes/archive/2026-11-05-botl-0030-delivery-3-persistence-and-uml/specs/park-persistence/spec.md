## ADDED Requirements

### Requirement: Delivery 3 elements persisted
The application SHALL persist and reconstruct the type of each attraction and every SpeedPass and Special Pass with its identifier, associated individual ticket (including composition tickets), stored price and consumed state, in a text file within the same data directory (and therefore also in `--fly` mode). Consumed passes SHALL remain stored while their ticket exists; passes removed by refunds or cascading ticket deletions SHALL no longer be stored. Occupants of attractions remain non-persistent.

#### Scenario: Passes reconstructed after restart
- **GIVEN** ticket `T001` with SpeedPasses `SP02:1`…`SP02:10` (one consumed) and Special Pass `PE01`, and composition ticket `TG01:12345678Z` with SpeedPass `SP03:1`
- **WHEN** the application restarts
- **THEN** all those passes exist with the same tickets and prices, `SP02:1` is still consumed and the rest unconsumed, and a second Special Pass for `T001` is still rejected

#### Scenario: Cascades and refunds reflected in the file
- **GIVEN** passes stored for future ticket `T001`
- **WHEN** `SPEEDPASS DELETE;SP02:2` and later `TICKET DELETE;T001` are processed and the application restarts
- **THEN** none of `T001`'s passes is loaded

#### Scenario: Attraction types reconstructed
- **GIVEN** attractions of types GENERAL, INDIVIDUAL, GROUP and SPECIAL
- **WHEN** the application restarts
- **THEN** each attraction has the same type and no occupants

#### Scenario: Fly mode covers passes
- **WHEN** passes are bought in a `--fly` execution
- **THEN** no `passes.txt` is written in the permanent data directory and the temporary directory is removed at the end
