# Spec Delta

## MODIFIED Requirements

### Requirement: Fly mode storage directory
When started with `--fly` as the first argument, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` identified by the start timestamp and SHALL make every repository (users, groups, tickets with their passes, attractions with their types) read and write only files inside it. Without `--fly` the permanent storage location and behaviour SHALL be unchanged.

#### Scenario: Isolated run
- **GIVEN** permanent data files exist
- **WHEN** the application runs with `--fly` and creates users, groups, tickets, and attractions
- **THEN** the data is written inside the per-run `tmp/` directory and the permanent files are not modified

#### Scenario: Fly run starts empty
- **GIVEN** permanent data contains user `ana@correo.com`
- **WHEN** the application runs with `--fly` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the deletion fails because the user does not exist in the fly storage

#### Scenario: F3 data isolated
- **GIVEN** permanent data files exist
- **WHEN** a `--fly` run buys passes, consumes them, and creates typed attractions, then exits
- **THEN** the permanent files are unchanged and the temporary directory is removed
