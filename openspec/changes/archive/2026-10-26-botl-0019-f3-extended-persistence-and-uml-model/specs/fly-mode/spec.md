# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary data directory
When `--fly` is given as the first argument, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (with a numeric suffix if that name already exists) and SHALL store and load every persistent element (users, groups, tickets, group tickets, attractions with their type, SpeedPasses and Special Passes, and any later persistent data) exclusively in files inside it. It starts with no data.

#### Scenario: Isolated run
- **WHEN** the application is run with `--fly` and creates users and tickets
- **THEN** the files written are inside `tmp/<timestamp>/` and the files in `data/` are not modified

#### Scenario: Passes in fly mode
- **WHEN** a `--fly` run buys passes
- **THEN** `passes.txt` is written only inside `tmp/<timestamp>/` and `data/passes.txt` is not modified
