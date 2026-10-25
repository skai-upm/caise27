# Spec Delta

## Purpose

Allows isolated runs that use all persistence features without altering the permanent data.

## ADDED Requirements

### Requirement: Fly mode uses a temporary directory
When started with the `--fly` option, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` at startup and SHALL store and read every persistent file (users, groups, tickets, group tickets, attractions) exclusively there, using the same repositories as normal mode configured with different paths.

#### Scenario: Permanent data untouched
- **WHEN** `java -jar target/parque.jar --fly` runs and creates a user
- **THEN** the user is stored under `tmp/<timestamp>/` and the permanent `data/` files are unchanged

#### Scenario: Fly mode starts empty
- **WHEN** permanent data contains users and the application starts with `--fly`
- **THEN** no users exist in that run

### Requirement: Temporary directory removed at the end
The temporary directory and all its files SHALL be deleted when the run ends, whether by `EXIT`, end of input or a file error.

#### Scenario: Deleted after EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists

### Requirement: Fly mode works with both input modes
`--fly` SHALL work in interactive mode and together with a command file argument.

#### Scenario: Fly with command file
- **WHEN** `java -jar target/parque.jar --fly input_fase2.txt` runs
- **THEN** the commands of `input_fase2.txt` are executed using the temporary directory

### Requirement: Normal mode unchanged
Without `--fly`, persistence SHALL keep using the permanent `data/` files exactly as before.

#### Scenario: Normal run
- **WHEN** the application runs without `--fly` and creates a user
- **THEN** the user is stored in `data/users.txt`
