# Spec Delta

## Purpose

Lets the application run with full persistence behavior against a throwaway, per-run directory, so isolated runs and tests never alter the normal persistent data.

## ADDED Requirements

### Requirement: Temporary data directory with --fly
When `--fly` is among the launch arguments, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>` (relative to the working directory, named with the run's start timestamp and made unique if that name already exists), initially empty, and every persistent element (users, groups, tickets, group tickets, attractions) SHALL be read from and written to files inside it only.

#### Scenario: Interactive fly run
- **GIVEN** persistent data files exist in the working directory
- **WHEN** the application is started with `--fly` and `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created in the run's temporary directory and the persistent files in the working directory are byte-for-byte unchanged

#### Scenario: Fly run with a command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are processed against the temporary data and the normal persistent data is unchanged

#### Scenario: Independent from normal data
- **GIVEN** a user stored in the normal persistent data
- **WHEN** a `--fly` run starts
- **THEN** that user does not exist in the fly run

### Requirement: Temporary directory removed at termination
When a `--fly` run terminates — by `EXIT`, end of input, a command-file error, or shutdown of the process — the run's temporary directory and all files in it SHALL be deleted.

#### Scenario: Removed after EXIT
- **WHEN** a `--fly` run processes several persistent commands and then `EXIT`
- **THEN** after the application ends its `tmp/<timestamp>` directory no longer exists

#### Scenario: Removed after a command-file error
- **WHEN** the application is started with `--fly missing.txt`
- **THEN** an error is reported and no run directory remains

### Requirement: Same repositories in both modes
Fly mode SHALL differ from normal execution only in the configured data directory; the same stores and business logic SHALL be used, and without `--fly` the persistent files in the working directory SHALL be used exactly as before.

#### Scenario: Normal run unaffected
- **WHEN** the application runs without `--fly`
- **THEN** it reads and writes `users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt` and `attractions.txt` in the working directory and creates no `tmp` run directory
