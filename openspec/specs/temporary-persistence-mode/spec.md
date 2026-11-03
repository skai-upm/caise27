# temporary-persistence-mode Specification

## Purpose

Allows isolated executions that use all persistence facilities without altering the application's permanent persistent data.

## Requirements

### Requirement: Temporary data directory
When launched with the `--fly` option, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/`, named after the start timestamp and distinct for every execution, and every persistent element (users, groups, tickets, group tickets, attractions with their type, and passes with their consumed status) SHALL be read from and written to files in that directory only. The run SHALL start with no stored data.

#### Scenario: Isolated interactive run
- **GIVEN** permanent data files with existing users
- **WHEN** the application runs with `--fly` and creates a user, a group and a ticket
- **THEN** the changes are written under `tmp/<timestamp>/` and the permanent files are byte-for-byte unchanged

#### Scenario: Isolated file run
- **GIVEN** a command file `input_fase2.txt`
- **WHEN** the application runs with `--fly input_fase2.txt`
- **THEN** its commands operate on the temporary data, exactly as they would in normal file mode

#### Scenario: Distinct directory per execution
- **GIVEN** a temporary directory of a previous run still exists with the same timestamp
- **WHEN** a new `--fly` run starts
- **THEN** it uses a different, new directory

#### Scenario: Delivery 3 data isolated
- **GIVEN** permanent data files
- **WHEN** a `--fly` run creates typed attractions, buys and consumes passes, and ends
- **THEN** the permanent files are unchanged and the temporary directory with its `passes.txt` and `attractions.txt` is deleted

### Requirement: Same repositories in both modes
Temporary mode SHALL use the same persistence components as normal mode, configured only with a different location; command behaviour SHALL be identical.

#### Scenario: Same results
- **GIVEN** the same command sequence run once normally on an empty data directory and once with `--fly`
- **WHEN** both runs finish
- **THEN** the printed responses are identical

### Requirement: Cleanup at termination
When an execution with `--fly` terminates — through `EXIT`, end of input or an invalid command file — the application SHALL delete its temporary directory together with all files in it.

#### Scenario: EXIT removes the directory
- **WHEN** a `--fly` run processes commands that write data and then `EXIT`
- **THEN** after termination its `tmp/<timestamp>/` directory no longer exists

#### Scenario: Missing command file
- **WHEN** the application runs with `--fly missing.txt`
- **THEN** the file error is reported and the temporary directory is deleted

### Requirement: Normal mode unchanged
Without `--fly`, the application SHALL keep using the permanent data files in the working directory exactly as before and SHALL NOT create a temporary directory.

#### Scenario: Ordinary run
- **WHEN** the application runs without `--fly`
- **THEN** data is stored in the permanent files and no `tmp/` entry is created
