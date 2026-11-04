# temporary-persistence-mode Specification

## Purpose
Allows isolated executions and tests that use every persistence facility of the application against a per-execution temporary directory, without altering the permanent data.

## Requirements

### Requirement: --fly option
The application SHALL accept the launch argument `--fly`, alone or together with a command file path, in interactive and file modes.

#### Scenario: Interactive fly run
- **WHEN** the application is launched with `--fly` and commands are typed on standard input
- **THEN** the commands are processed exactly as in a normal interactive run

#### Scenario: File fly run
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are processed in order, as in a normal file run

### Requirement: Per-execution temporary directory
With `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` under the working directory, identified by the execution's start timestamp and distinct for each execution, initially empty.

#### Scenario: Directory created at startup
- **GIVEN** an execution started at 2026-08-26 19:03:15 with `--fly`
- **WHEN** the application is running
- **THEN** the directory `tmp/20260826_190315/` exists and holds the data files written during the run

#### Scenario: Two executions in the same second
- **GIVEN** `tmp/20260826_190315/` already exists
- **WHEN** another `--fly` execution starts in that same second
- **THEN** it uses a different directory (`tmp/20260826_190315_1/`)

### Requirement: All persistence redirected
During a `--fly` execution every repository (users, groups, tickets, group tickets, attractions and any other persistent element) SHALL read and write only files inside the temporary directory, through the same repository implementations as a normal execution; the permanent data files SHALL NOT be read or modified.

#### Scenario: Permanent data untouched
- **GIVEN** permanent data files with existing users
- **WHEN** a `--fly` run creates, updates and deletes users, groups, tickets and attractions
- **THEN** the permanent data files are byte-for-byte unchanged after the run

#### Scenario: Independent starting state
- **GIVEN** the permanent data contains user `ana@correo.com`
- **WHEN** a `--fly` run processes `USER CREATE` for a user with email `ana@correo.com`
- **THEN** the creation succeeds, because the temporary data starts empty

### Requirement: Temporary directory deleted at termination
When a `--fly` execution terminates — by `EXIT`, end of input, a command-file error or JVM shutdown — the temporary directory and all the files it contains SHALL be deleted, together with the `tmp/` directory if it is left empty.

#### Scenario: Deleted after EXIT
- **WHEN** a `--fly` run processes commands that write data and then `EXIT`
- **THEN** after termination its `tmp/<timestamp>/` directory no longer exists

#### Scenario: Deleted after a file error
- **WHEN** the application is launched with `--fly missing.txt` and the file does not exist
- **THEN** an `ERROR:` is reported, the exit status is 1, and no temporary directory remains

### Requirement: Normal mode unchanged
Without `--fly` the application SHALL use the permanent data files in the working directory exactly as before and SHALL NOT create a `tmp/` directory.

#### Scenario: Normal run
- **WHEN** the application is launched without `--fly` and creates a user
- **THEN** the user is written to the permanent `users.txt` and is available in the next normal run
