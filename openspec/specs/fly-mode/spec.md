# fly-mode Specification

## Purpose

Allows the application to be run in isolation with full persistence features while leaving the permanent data untouched, by redirecting all persistence files to a temporary directory deleted at the end of the run.

## Requirements

### Requirement: Temporary data directory
When `--fly` is given as the first argument, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (with a numeric suffix if that name already exists) and SHALL store and load every persistent element (users, groups, tickets, group tickets, attractions and any later persistent data) exclusively in files inside it. It starts with no data.

#### Scenario: Isolated run
- **WHEN** the application is run with `--fly` and creates users and tickets
- **THEN** the files written are inside `tmp/<timestamp>/` and the files in `data/` are not modified

### Requirement: Cleanup at termination
The temporary directory and all files in it SHALL be deleted when a `--fly` execution ends, whether it ends with `EXIT`, at the end of the input, or by interruption of the process.

#### Scenario: Ends with EXIT
- **WHEN** a `--fly` run processes `EXIT`
- **THEN** the run's `tmp/<timestamp>/` directory no longer exists after the process ends

### Requirement: Compatible with execution modes
`--fly` SHALL work in interactive mode (`--fly`) and in file mode (`--fly <commandFile>`), with the same command processing as normal runs.

#### Scenario: Command file in fly mode
- **WHEN** the application is run with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are executed using the temporary directory

### Requirement: Normal persistence unchanged
Without `--fly`, the application SHALL keep using the permanent `data/` directory exactly as before.

#### Scenario: Normal run
- **WHEN** the application is run without `--fly`
- **THEN** data is loaded from and saved to `data/` and persists between executions
