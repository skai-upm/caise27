# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 state persisted
Persistence SHALL preserve and reconstruct the type of each attraction, SpeedPasses and Special Passes with the individual tickets they belong to (including tickets inside group tickets), the stored price of each pass and its consumed state. Consumed passes SHALL remain stored while their ticket exists; passes removed by valid ticket deletions SHALL not be reconstructed. The `--fly` mode SHALL cover these elements with the same repositories, and attraction occupants SHALL NOT be persisted.

#### Scenario: Restart through the application
- **WHEN** an execution creates typed attractions and passes, consumes some passes by entering attractions, deletes a future ticket with passes, and the application restarts in normal mode
- **THEN** types, pass tickets, prices and consumed states are as before, the deleted ticket's passes do not exist, and all attractions are empty

#### Scenario: Fly mode with Delivery 3 elements
- **WHEN** the same command file is executed with `--fly`
- **THEN** it behaves the same and no data remains afterwards
