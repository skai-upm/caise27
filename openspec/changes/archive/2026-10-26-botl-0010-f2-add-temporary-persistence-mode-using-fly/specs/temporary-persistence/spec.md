# Spec Delta

## Purpose

Allows isolated executions that use every persistence feature against a per-run temporary directory, leaving the permanent data untouched.

## ADDED Requirements

### Requirement: Temporary data directory
When `--fly` is among the startup arguments, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` named after the start time (adding a numeric suffix if that name already exists) and SHALL use it as the only location of all persistence files for that execution. The permanent data files SHALL NOT be read or modified.

#### Scenario: Isolated execution
- **WHEN** permanent data contains user Ana and the application runs with `--fly` and executes `USER CREATE` for Pedro
- **THEN** Pedro is stored in a file inside `tmp/<timestamp>/`, Ana is not visible during the run, and the permanent user file is unchanged

#### Scenario: Persistence works within the run
- **WHEN** in a `--fly` run a user is created and a later command in the same run references it
- **THEN** the user exists and is stored in the temporary directory

### Requirement: Temporary directory removed at termination
The temporary directory and all its files SHALL be deleted when a `--fly` execution terminates, whether by `EXIT`, end of standard input, end of the command file, or an error opening or reading the command file.

#### Scenario: EXIT
- **WHEN** a `--fly` run receives `EXIT`
- **THEN** after the application terminates, the directory created for that run no longer exists

#### Scenario: End of command file
- **WHEN** the application runs with `--fly input_fase2.txt` and the file has no `EXIT`
- **THEN** after processing the file the temporary directory no longer exists

### Requirement: Fly mode with both input sources
The `--fly` flag SHALL be combinable with interactive mode (`--fly` alone) and with file mode (`--fly <file>`); the command file is the first argument that is not `--fly`.

#### Scenario: Fly with file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed against the temporary directory

### Requirement: Permanent mode unchanged
Without `--fly`, the application SHALL keep using the permanent `data/` directory exactly as before and SHALL NOT create any temporary directory.

#### Scenario: Normal run
- **WHEN** the application is started without `--fly`
- **THEN** users created are stored in `data/` and remain after the application ends
