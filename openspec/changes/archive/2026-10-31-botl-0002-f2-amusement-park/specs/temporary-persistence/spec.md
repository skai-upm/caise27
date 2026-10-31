# Spec Delta

## Purpose

Provides an isolated `--fly` execution mode in which every persistence facility works against a throw-away directory, so runs and tests never alter the permanent data.

## ADDED Requirements

### Requirement: Temporary storage directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique per execution) and SHALL store all persistent data of that run (users, groups, tickets, group tickets, attractions) exclusively in it.

#### Scenario: Data goes to the temporary directory
- **WHEN** the application runs with `--fly` and creates a user
- **THEN** the user is written under the run's `tmp/<timestamp>/` directory and the permanent user file is not modified

### Requirement: Shared repository logic
Normal and `--fly` executions SHALL use the same repository implementations and business behavior; only the configured storage paths SHALL differ.

#### Scenario: Same behavior in both modes
- **WHEN** the same command sequence is run once normally and once with `--fly`, each starting from empty storage
- **THEN** the command outputs are identical

### Requirement: Cleanup on termination
On termination, including via `EXIT`, end of input or an unreadable command file, the application SHALL delete the run's temporary directory and all files in it. A `--fly` run SHALL leave the permanent data unchanged.

#### Scenario: Isolated run and cleanup
- **WHEN** permanent data exists, a `--fly` session creates and deletes users and exits with `EXIT`
- **THEN** the run's temporary directory no longer exists and a following normal run sees the original permanent data

### Requirement: Fly mode with both input modes
`--fly` SHALL work interactively (`--fly` alone) and with a command file (`--fly <file>`).

#### Scenario: Fly with a command file
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are executed against temporary storage, which is removed afterwards
