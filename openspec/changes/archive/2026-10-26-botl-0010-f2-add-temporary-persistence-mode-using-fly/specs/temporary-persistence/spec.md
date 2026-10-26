# Spec Delta

## Purpose

Provides an execution mode in which all persistence happens in an isolated temporary directory that is removed at the end of the run, so tests and experiments never alter the permanent data.

## ADDED Requirements

### Requirement: Temporary data directory with --fly
When the application is started with the `--fly` option, it SHALL create at start-up a new directory under `tmp/` named with the start timestamp `yyyyMMdd_HHmmss` (with a numeric suffix if that name already exists), and every persistent element of the system SHALL be read from and written to files inside that directory only.

#### Scenario: Isolated run
- **WHEN** `java -jar target/parque.jar --fly` is run and `USER CREATE;...` is processed
- **THEN** the user is stored only inside `tmp/<timestamp>/` and the permanent `data/` files are not modified

#### Scenario: Fresh state
- **WHEN** a `--fly` run starts while the permanent data contains users
- **THEN** the run starts without those users, because its data directory is new and empty

### Requirement: Temporary directory removed at termination
The temporary directory created by `--fly` and all files in it SHALL be deleted when the application terminates, whether by `EXIT`, by the end of the input, or by the JVM shutting down.

#### Scenario: Deleted after EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** after the application ends, its `tmp/<timestamp>/` directory no longer exists

### Requirement: Fly mode works with both input modes
`--fly` SHALL be combinable with interactive execution (`--fly`) and with execution from a command file (`--fly input_fase2.txt`).

#### Scenario: Fly with a command file
- **WHEN** `java -jar target/parque.jar --fly input_fase2.txt` is run
- **THEN** the commands in the file are processed using the temporary directory

### Requirement: Same repositories in both modes
Normal and temporary modes SHALL use the same repository implementations; the only difference SHALL be the configured file locations. Without `--fly`, the existing permanent persistence behaviour SHALL be preserved exactly.

#### Scenario: Normal run unchanged
- **WHEN** the application is run without `--fly`
- **THEN** data is read from and written to `data/` as before
