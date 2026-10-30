# Spec Delta

## Purpose

Allows isolated runs that use all persistence features without touching the permanent data of normal runs, by redirecting storage to a per-run temporary directory.

## ADDED Requirements

### Requirement: Per-run temporary storage directory
When `--fly` is given, the application SHALL create at startup a directory `tmp/<yyyyMMdd_HHmmss>/` (with a numeric suffix if it already exists) and SHALL use it as the location of every persistence file (users, groups, tickets, group tickets, attractions). The same repository code SHALL be used as in normal mode; only the storage paths differ.

#### Scenario: Isolation from permanent data
- **GIVEN** permanent data with user Ana exists
- **WHEN** a `--fly` run creates user Pedro and exits
- **THEN** the permanent files are byte-for-byte unchanged and a later normal run does not know Pedro

#### Scenario: Same behavior as normal mode
- **WHEN** the same command sequence runs once normally from an empty state and once with `--fly`
- **THEN** the outputs are identical

### Requirement: Temporary directory cleanup
On termination of a `--fly` run, whether by `EXIT`, end of input, or the end of a command file, the per-run temporary directory and all its files SHALL be deleted.

#### Scenario: Cleanup after EXIT
- **WHEN** a `--fly` run processes `USER CREATE;...` and `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists after the run

### Requirement: Fly mode with both input modes
`--fly` SHALL work with interactive input (`--fly`) and with a command file (`--fly <file>`).

#### Scenario: Fly with command file
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed using temporary storage
