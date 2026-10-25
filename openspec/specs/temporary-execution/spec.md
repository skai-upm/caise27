# temporary-execution Specification

## Purpose

Offers an isolated execution mode whose persistence lives in a temporary directory removed at the end, so runs and tests do not alter permanent data.

## Requirements

### Requirement: Fly mode activation
When `--fly` is among the program arguments, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (adding a numeric suffix if it already exists) and use it as the location of every persistence file for that run. The remaining first argument, if any, SHALL be the command file.

#### Scenario: Fly with command file
- **WHEN** `java -jar target/parque.jar --fly input_fase2.txt` runs commands that create users, groups, tickets and attractions
- **THEN** all those files are written inside the run's `tmp/<timestamp>/` directory and the normal data directory is not created or modified

#### Scenario: Fly interactive
- **WHEN** the application is started with only `--fly` and receives commands on standard input
- **THEN** commands are processed exactly as in normal interactive mode using the temporary directory

### Requirement: Same repositories, different paths
Fly mode SHALL use the same persistence classes as normal mode, configured only with different file paths.

#### Scenario: Data visible within the run
- **WHEN** in fly mode a user is created and then a ticket is bought for that user in the same run
- **THEN** the ticket purchase succeeds

### Requirement: Temporary directory removal
On termination — via `EXIT`, end of input, a command-file error or process shutdown — the run's temporary directory and all its files SHALL be deleted.

#### Scenario: Removed after EXIT
- **WHEN** a fly run ends with `EXIT`
- **THEN** the run's `tmp/<timestamp>/` directory no longer exists

### Requirement: Normal mode unchanged
Without `--fly`, persistence SHALL keep using the permanent data directory exactly as before.

#### Scenario: Normal run
- **WHEN** the application runs without `--fly` and creates a user
- **THEN** the user is stored in the permanent data directory and remains after restart
