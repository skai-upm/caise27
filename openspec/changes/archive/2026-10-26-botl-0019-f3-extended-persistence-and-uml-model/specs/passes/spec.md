# Spec Delta

## MODIFIED Requirements

### Requirement: Pass persistence
Passes SHALL be stored with their kind, identifier, associated individual ticket, price and consumed status and restored on start-up attached to their tickets, in normal and `--fly` modes. Passes removed by ticket deletions SHALL be removed from storage as well.

#### Scenario: Restart
- **WHEN** T001 has two SpeedPasses (one consumed) and a Special Pass and the application restarts
- **THEN** the three passes are restored with the same prices and statuses

#### Scenario: Fly mode
- **WHEN** passes are bought and consumed during a `--fly` run
- **THEN** they are stored only in the temporary directory and the permanent data is unchanged

#### Scenario: Cascade persisted
- **WHEN** a ticket with passes is deleted and the application restarts
- **THEN** none of its passes is restored
