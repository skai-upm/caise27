# Spec Delta

## MODIFIED Requirements

### Requirement: Pass persistence
Passes SHALL be stored with identifier, type, associated individual ticket, price and consumption status, and restored at startup attached to their tickets with the same price and consumption status. Pass identifiers SHALL stay unique across executions. In `--fly` executions passes SHALL be stored only in the run's temporary directory, like every other persistent element.

#### Scenario: Passes survive restart
- **WHEN** a SpeedPass and a Special Pass are bought, one is consumed, and the application restarts
- **THEN** both passes exist with the same ticket, price and consumption status, and new passes get identifiers not used before

#### Scenario: Passes of internal tickets
- **WHEN** a pass bought for internal ticket `TG01-1` is consumed and the application restarts
- **THEN** it is restored attached to `TG01-1` inside `TG01`, still consumed

#### Scenario: Fly mode
- **WHEN** passes are bought and consumed during a `--fly` run
- **THEN** the permanent passes file is not modified
