# temporary-execution Specification

## Purpose

Allows isolated executions that use all persistence facilities against temporary files, leaving the normal persistent data untouched.

## Requirements

### Requirement: --fly creates a temporary data directory
When the application is started with the `--fly` option, it SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (distinct for each execution) and every repository SHALL read and write all its persistence files only in that directory for the whole run.

#### Scenario: Isolated run
- **WHEN** `java -jar target/parque.jar --fly` is run and a user is created
- **THEN** the normal `data/` files are not modified

### Requirement: Temporary directory removed at the end
When a `--fly` execution terminates, including through `EXIT` or end of input, the temporary directory and all its files SHALL be deleted.

#### Scenario: Cleanup after EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists

### Requirement: Compatible with both modes
`--fly` SHALL work in interactive mode (`--fly` alone) and in file mode (`--fly <file>`), using the same repositories configured with different paths. Without `--fly`, the permanent persistence behavior SHALL be unchanged.

#### Scenario: File mode with --fly
- **WHEN** `java -jar target/parque.jar --fly input_fase2.txt` is run
- **THEN** the commands in the file are executed against temporary data
