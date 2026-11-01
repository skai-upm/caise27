# temporary-persistence Specification

## Purpose

Provides an isolated `--fly` execution mode in which all persistence uses a per-run temporary directory, so tests and trials never alter the normal persistent data.

## Requirements

### Requirement: Per-run temporary directory
When started with `--fly`, the application SHALL create at startup a directory `tmp/<yyyyMMdd_HHmmss>/` identified by the start timestamp and unique for that run, and every repository (users, groups, tickets, group tickets, attractions) SHALL read and write only files inside it.

#### Scenario: Temporary directory used
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is stored under `tmp/<timestamp>/` and the normal data files are untouched

### Requirement: Shared repositories
Normal and `--fly` executions SHALL use the same repository implementations; only the configured storage paths SHALL differ. Without `--fly` the existing permanent-persistence behavior SHALL be preserved exactly.

#### Scenario: Shared functionality
- **GIVEN** equivalent commands run once normally and once with `--fly`
- **WHEN** the commands execute
- **THEN** the replies are identical while the storage locations differ

### Requirement: Cleanup on termination
When a `--fly` execution terminates, including through `EXIT`, end of input or an unreadable command file, the application SHALL delete its temporary directory and all files in it.

#### Scenario: Isolated run and cleanup
- **GIVEN** normal persistent data already exists
- **WHEN** a `--fly` session adds or modifies state and exits
- **THEN** its temporary directory no longer exists and a subsequent normal run observes the original permanent state

### Requirement: Fly mode with both input modes
`--fly` SHALL work for interactive execution (`--fly`) and command-file execution (`--fly <file>`).

#### Scenario: Fly with command file
- **WHEN** the application runs `--fly input_fase2.txt`
- **THEN** the file's commands execute against temporary storage and the directory is removed afterwards
