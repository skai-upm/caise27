# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary storage directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique per execution) and SHALL store all persistent data of that run (users, groups, tickets, group tickets, passes, attractions) exclusively in it.

#### Scenario: Data goes to the temporary directory
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is written under the run's `tmp/<timestamp>/` directory and the permanent user file is not modified

#### Scenario: Passes isolated
- **WHEN** a `--fly` run buys passes and ends
- **THEN** no permanent `passes.txt` is created or changed
