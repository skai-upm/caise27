# temporary-execution Specification

## Purpose

Allows isolated runs that use all persistence features against a throw-away directory, so tests and demos never alter the normal persistent data.

## Requirements

### Requirement: Fly mode directory
When the arguments include `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique if it already exists) and SHALL use it as the location of every persistence file (users, groups, tickets, group tickets, attractions) for that run, starting from empty data. The same repository code SHALL be used; only the configured paths differ.

#### Scenario: Isolated persistence
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is stored only inside the run's `tmp/<timestamp>/` directory and the normal `users.txt` is not modified

### Requirement: Fly mode cleanup
The temporary directory and all its files SHALL be deleted when the application terminates, including termination via `EXIT`, end of input, a command-file error, or process shutdown.

#### Scenario: Deleted on EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists after the application ends

### Requirement: Fly mode with both input modes
`--fly` SHALL work in interactive mode (`--fly` alone) and in file mode (`--fly <file>`). Without `--fly`, persistence SHALL behave exactly as before.

#### Scenario: Fly with command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the file's commands are executed against the temporary directory, which is deleted afterwards
