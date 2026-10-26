# Spec Delta

## Purpose

Defines the `--fly` execution mode, in which the application uses a fresh timestamped directory for all persistence files during one run and deletes it when the run ends, leaving the permanent data untouched.

## ADDED Requirements

### Requirement: Temporary data directory
When started with `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` named after the start time of the run (adding a numeric suffix if that name already exists), and it SHALL start with no stored data.

#### Scenario: Directory created at startup
- **WHEN** the application is started with `--fly` at 2026-08-26 19:03:15
- **THEN** the directory `tmp/20260826_190315/` exists while the run is in progress

#### Scenario: Empty data
- **WHEN** users exist in the permanent data and the application is started with `--fly`
- **THEN** no users exist during that run

### Requirement: Exclusive use by every repository
During a `--fly` run every repository — users, groups, tickets, group tickets, attractions and any other persistent element — SHALL read and write only files inside the temporary directory, using the same repository implementation as normal execution configured with that directory. The permanent data files SHALL NOT be read or modified.

#### Scenario: Permanent data untouched
- **WHEN** users, groups, tickets and attractions are created during a `--fly` run
- **THEN** their files are written in the temporary directory and the files under `data/` are unchanged

#### Scenario: Persistence works within the run
- **WHEN** a user is created and then updated during a `--fly` run
- **THEN** the update is applied to the user stored in the temporary directory

### Requirement: Temporary directory removal
When a `--fly` run terminates — at the end of standard input, when `EXIT` is processed, at the end of the command file, when the input file is invalid, or when the process is stopped — the temporary directory created for that run SHALL be deleted together with all its files.

#### Scenario: Deleted after EXIT
- **WHEN** `EXIT` is processed in a `--fly` run
- **THEN** the run's temporary directory no longer exists after the application terminates

#### Scenario: Deleted after a command file
- **WHEN** `java -jar target/parque.jar --fly input_fase2.txt` finishes processing the file
- **THEN** the run's temporary directory no longer exists

### Requirement: Normal mode unchanged
Without `--fly`, the application SHALL keep using the permanent data files exactly as before and SHALL NOT create any temporary directory.

#### Scenario: Normal execution
- **WHEN** the application runs without `--fly` and creates a user
- **THEN** the user is stored in the permanent data and survives restarts, and no `tmp/` directory is created
