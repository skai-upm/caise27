# temporary-execution Specification

## Purpose

Provides the `--fly` execution mode, which runs the application with all persistence redirected to a temporary directory that is removed at the end, leaving permanent data untouched.

## Requirements

### Requirement: Temporary persistence directory
When started with the `--fly` option, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` identified by the start timestamp, and SHALL use it as the only location of every persistence file (users, groups, tickets, group tickets, attractions) during that run.

#### Scenario: Permanent data untouched
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is written under the run's `tmp/<timestamp>/` directory and the permanent data files are not modified

#### Scenario: Distinct directory per run
- **WHEN** a `--fly` run starts while a directory with the same timestamp already exists
- **THEN** the run uses a different, new directory

### Requirement: Same persistence logic
The `--fly` mode SHALL reuse the same persistence implementation as normal execution, differing only in the configured file locations.

#### Scenario: Data reload within the run
- **WHEN** in a `--fly` run data is saved and read back by the persistence components
- **THEN** it behaves exactly as in normal execution

### Requirement: Cleanup at termination
When a `--fly` run ends — through `EXIT`, end of input, an input-file error, or interruption of the process — the temporary directory and all its files SHALL be deleted.

#### Scenario: Cleanup after EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists after the application ends

### Requirement: Compatible with both execution modes
`--fly` SHALL work with interactive execution (`--fly` alone) and with a command file (`--fly <file>`).

#### Scenario: Fly with a command file
- **WHEN** the application is run as `java -jar target/parque.jar --fly input_fase2.txt`
- **THEN** the file's commands are executed using temporary persistence

### Requirement: Normal mode unchanged
Without `--fly`, the application SHALL keep the permanent persistence behavior unchanged.

#### Scenario: Normal run
- **WHEN** the application runs without `--fly`
- **THEN** data is read from and written to the permanent data files
