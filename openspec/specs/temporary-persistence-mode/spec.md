# temporary-persistence-mode Specification

## Purpose
Lets the application run with all of its persistence redirected to a per-execution temporary directory, so isolated runs and tests never alter the permanent data.

## Requirements

### Requirement: Temporary data directory
When the `--fly` option is given, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` under the working directory, named after the start time. If that directory already exists, a numeric suffix (`_1`, `_2`, ...) SHALL be appended so each execution gets its own directory. The data files SHALL start empty.

#### Scenario: Directory created at startup
- **GIVEN** the application starts with `--fly` at 2026-08-26 19:03:15
- **WHEN** the first command is processed
- **THEN** the directory `tmp/20260826_190315/` exists and holds the run's data files

#### Scenario: Two runs in the same second
- **GIVEN** `tmp/20260826_190315/` already exists
- **WHEN** another `--fly` execution starts at the same second
- **THEN** it uses `tmp/20260826_190315_1/`

### Requirement: All persistence redirected
In `--fly` mode every persistent element — users, groups, tickets, group tickets, attractions (with their types) and passes — SHALL be read from and written to files inside the temporary directory only, using the same persistence components as a normal execution. The permanent files SHALL NOT be read or modified.

#### Scenario: Permanent data untouched
- **GIVEN** permanent files with user Ana
- **WHEN** a `--fly` execution creates Pedro and ends
- **THEN** the permanent files are byte-for-byte unchanged and the `--fly` run did not see Ana

#### Scenario: Commands operate on temporary data
- **WHEN** the application runs `--fly input_fase2.txt` where the file creates and then updates a user
- **THEN** the update succeeds because the created user is persisted in the temporary directory during the run

#### Scenario: Delivery 3 data isolated
- **WHEN** a `--fly` run creates an attraction of type `GROUP`, a ticket and SpeedPasses
- **THEN** the passes and typed attraction are written only inside the run directory and the permanent `passes.txt` and `attractions.txt` are unchanged

### Requirement: Cleanup on termination
When a `--fly` execution terminates — after `EXIT`, at end of input, after a command-file error, or on JVM shutdown — the temporary directory of that execution and all files in it SHALL be deleted. Other executions' directories SHALL NOT be touched.

#### Scenario: EXIT
- **WHEN** a `--fly` file run ends with `EXIT`
- **THEN** its `tmp/<timestamp>/` directory no longer exists

#### Scenario: Missing command file
- **WHEN** the application is launched with `--fly missing.txt`
- **THEN** the error is reported and the temporary directory is deleted

### Requirement: Normal mode unchanged
Without `--fly`, the application SHALL use the permanent files in the working directory exactly as before and SHALL NOT create a `tmp` directory.

#### Scenario: Normal execution
- **WHEN** the application runs without `--fly`
- **THEN** changes are written to the permanent files and no `tmp` directory is created
