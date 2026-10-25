# Spec Delta

## MODIFIED Requirements

### Requirement: Fly mode uses a temporary directory
When started with the `--fly` option, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` at startup and SHALL store and read every persistent file (users, groups, tickets, group tickets, passes, attractions) exclusively there, using the same repositories as normal mode configured with different paths.

#### Scenario: Permanent data untouched
- **WHEN** `java -jar target/parque.jar --fly` runs and creates a user
- **THEN** the user is stored under `tmp/<timestamp>/` and the permanent `data/` files are unchanged

#### Scenario: Fly mode starts empty
- **WHEN** permanent data contains users and the application starts with `--fly`
- **THEN** no users exist in that run

#### Scenario: Passes in fly mode
- **WHEN** a `--fly` run buys passes
- **THEN** the passes file is written under `tmp/<timestamp>/` and the permanent `data/` directory is unchanged
