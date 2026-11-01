# Spec Delta

## Purpose

Allows isolated application runs that use every persistence facility without altering the permanent data of normal runs.

## ADDED Requirements

### Requirement: Fly mode storage directory
When started with `--fly` as the first argument, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` identified by the start timestamp and SHALL make every repository (users, groups, tickets, attractions) read and write only files inside it. Without `--fly` the permanent storage location and behaviour SHALL be unchanged.

#### Scenario: Isolated run
- **GIVEN** permanent data files exist
- **WHEN** the application runs with `--fly` and creates users, groups, tickets, and attractions
- **THEN** the data is written inside the per-run `tmp/` directory and the permanent files are not modified

#### Scenario: Fly run starts empty
- **GIVEN** permanent data contains user `ana@correo.com`
- **WHEN** the application runs with `--fly` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the deletion fails because the user does not exist in the fly storage

### Requirement: Shared repository logic
Repositories SHALL use the same data-access code in normal and fly modes; only the configured storage paths SHALL differ.

#### Scenario: Shared functionality
- **GIVEN** equivalent commands run once normally and once with `--fly` from equivalent initial state
- **WHEN** the commands execute
- **THEN** the outputs are identical while the files are written to different directories

### Requirement: Fly mode cleanup
On termination of a fly run — by `EXIT`, end of input, end of the command file, or an unreadable command file — the application SHALL delete the per-run directory and all its files.

#### Scenario: Isolated run and cleanup
- **GIVEN** normal persistent data already exists
- **WHEN** a `--fly` session adds or modifies state and exits with `EXIT`
- **THEN** its temporary directory is removed, and a subsequent normal run observes the original permanent state

### Requirement: Fly mode with both input modes
`--fly` SHALL work interactively (`java -jar target/parque.jar --fly`) and with a command file given as the next argument (`java -jar target/parque.jar --fly input_fase2.txt`).

#### Scenario: Fly with command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are executed against fly storage and the directory is removed at the end
