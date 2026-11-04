# temporary-persistence-mode Specification

## Purpose

Lets the application run with all its persistence redirected to a temporary, per-run directory that is removed at the end, so isolated runs and tests never alter the permanent data.

## Requirements

### Requirement: Temporary data directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` next to the permanent data files, named with the start timestamp and distinct from any existing directory, and SHALL make every repository (users, groups, tickets, group tickets, attractions) read and write only files inside it. The temporary directory SHALL start without data files.

#### Scenario: All repositories use the temporary directory
- **GIVEN** permanent data files exist with users, groups, tickets and attractions
- **WHEN** the application starts with `--fly` and processes commands that create a user, a group, a ticket, a group ticket and an attraction
- **THEN** all the resulting data files are written inside `tmp/<timestamp>/` and the permanent data files are byte-for-byte unchanged

#### Scenario: Independent start
- **GIVEN** a permanent users file containing Ana
- **WHEN** the application starts with `--fly` and processes `USER CREATE` for Ana with the same DNI and email
- **THEN** the creation succeeds, because the temporary run has no stored users

#### Scenario: Directory name
- **WHEN** the application starts with `--fly`
- **THEN** the directory created under `tmp/` has a name of the form `yyyyMMdd_HHmmss` (with a numeric suffix only if that name already exists)

### Requirement: Temporary data removed at termination
When a `--fly` run ends, whether by `EXIT`, by end of standard input or of the command file, or because the command file cannot be read, the application SHALL delete the run's temporary directory and every file in it.

#### Scenario: EXIT deletes the directory
- **WHEN** a `--fly` run creates users and then processes `EXIT`
- **THEN** after the application ends, its `tmp/<timestamp>/` directory no longer exists

#### Scenario: Unreadable command file
- **WHEN** the application is started with `--fly missing.txt` and `missing.txt` does not exist
- **THEN** it reports an `ERROR` and its temporary directory no longer exists after it ends

### Requirement: Same behaviour in both input modes
`--fly` SHALL work in interactive mode (`--fly` alone) and in file mode (`--fly <file>`), using the same command processing. Apart from the data location, the output of the same commands SHALL be identical to a normal run.

#### Scenario: File mode with --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are processed against the temporary data and their output equals that of a normal run starting with no data

### Requirement: Normal runs unchanged
Without `--fly`, the application SHALL use the permanent data files exactly as before and SHALL NOT create any temporary directory.

#### Scenario: Normal run
- **WHEN** the application runs without `--fly`
- **THEN** it reads and writes the permanent data files and no `tmp/` subdirectory is created
