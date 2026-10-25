# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary persistence directory
When started with the `--fly` option, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` identified by the start timestamp, and SHALL use it as the only location of every persistence file (users, groups, tickets, group tickets, passes, attractions) during that run.

#### Scenario: Permanent data untouched
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is written under the run's `tmp/<timestamp>/` directory and the permanent data files are not modified

#### Scenario: Distinct directory per run
- **WHEN** a `--fly` run starts while a directory with the same timestamp already exists
- **THEN** the run uses a different, new directory

#### Scenario: Passes in fly mode
- **WHEN** a `--fly` run buys passes
- **THEN** pass data is written only inside the run's temporary directory
