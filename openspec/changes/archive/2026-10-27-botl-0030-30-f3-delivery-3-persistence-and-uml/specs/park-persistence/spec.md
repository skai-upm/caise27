# Spec Delta

## ADDED Requirements

### Requirement: Passes persisted
SpeedPasses and Special Passes SHALL be stored in a text file with their identifier, type, associated individual ticket, stored price and consumed state, and rebuilt at start-up linked to the loaded individual tickets (including tickets inside group tickets); consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Passes survive restart
- **WHEN** `SP02:1`…`SP02:10` (8.00 each) for `T001` and Special Pass `PE01` for `TG01:12345678Z` are bought and the application restarts
- **THEN** all passes exist, linked to their tickets, with the same prices and unconsumed

#### Scenario: Consumed state survives restart
- **WHEN** a SpeedPass is consumed by `ATTRACTION ENTER;A01;T001;SPEED` and the application restarts
- **THEN** that pass is still stored and still consumed, so it cannot be refunded

#### Scenario: Refunds and cascades persisted
- **WHEN** a pass is refunded, or a future ticket with passes is deleted, and the application restarts
- **THEN** the removed passes no longer exist

### Requirement: Temporary mode covers passes
In `--fly` mode passes SHALL be stored only inside the temporary directory, using the same repository and file format as normal mode.

#### Scenario: Fly run with passes
- **WHEN** a `--fly` run buys passes
- **THEN** the permanent data files are unchanged and nothing remains after the run
