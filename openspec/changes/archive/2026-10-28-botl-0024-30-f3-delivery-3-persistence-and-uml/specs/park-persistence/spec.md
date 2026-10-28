## ADDED Requirements

### Requirement: Delivery 3 state in both persistence modes
Attraction types and passes (with their individual ticket, stored price and consumed state) SHALL be persisted and reconstructed in normal mode and handled through the same repositories, configured with the temporary directory, in `--fly` mode. Consumed passes SHALL remain stored while their ticket exists, and valid ticket deletions SHALL remove the associated passes from the stored state.

#### Scenario: Consumed pass and typed attraction after restart
- **WHEN** an execution creates a SPECIAL attraction, buys a Special Pass, consumes it by entering, and the application restarts
- **THEN** the attraction is still SPECIAL, the pass still exists with its price and consumed state, and the attraction has no occupants

#### Scenario: Cascading deletion persisted
- **WHEN** a future ticket with passes is deleted and the application restarts
- **THEN** neither the ticket nor its passes are restored

#### Scenario: Fly mode leaves no Delivery 3 data
- **WHEN** the same command file is run with `--fly`
- **THEN** it produces the same output as in normal mode on an empty data directory and no temporary directory remains
