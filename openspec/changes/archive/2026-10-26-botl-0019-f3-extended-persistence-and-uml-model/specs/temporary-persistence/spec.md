# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary data directory
When `--fly` is among the startup arguments, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` named after the start time (adding a numeric suffix if that name already exists) and SHALL use it as the only location of all persistence files for that execution, including users, groups, tickets, group tickets, passes and attractions. The permanent data files SHALL NOT be read or modified.

#### Scenario: Isolated execution
- **WHEN** permanent data contains user Ana and the application runs with `--fly` and executes `USER CREATE` for Pedro
- **THEN** Pedro is stored in a file inside `tmp/<timestamp>/`, Ana is not visible during the run, and the permanent user file is unchanged

#### Scenario: Persistence works within the run
- **WHEN** in a `--fly` run a user is created and a later command in the same run references it
- **THEN** the user exists and is stored in the temporary directory

#### Scenario: Passes and attraction types in fly mode
- **WHEN** a `--fly` run buys SpeedPasses and creates a typed attraction
- **THEN** the pass and attraction files are written only inside the temporary directory and the permanent pass and attraction files are unchanged
