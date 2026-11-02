# temporary-persistence Specification

## Purpose

Provides an isolated execution mode whose persistence files live in a per-run temporary directory that is removed at the end, leaving permanent data untouched.

## Requirements

### Requirement: Fly mode uses an isolated directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique if it already exists) and SHALL store and load the files of every repository — users, groups, tickets, group tickets, and attractions — only there. The same repository code SHALL be used as in normal mode; only the paths differ.

#### Scenario: Isolated run and cleanup
- **WHEN** permanent data exists and a `--fly` session creates users, groups, tickets, and attractions and then exits
- **THEN** a subsequent normal run observes exactly the original permanent data

#### Scenario: Shared functionality
- **WHEN** the same commands run once normally and once with `--fly` from equivalent empty state
- **THEN** the command outputs are identical

### Requirement: Fly mode cleanup on termination
On termination of a `--fly` run — by `EXIT`, end of input, or end of the command file — the application SHALL delete the temporary directory and all files in it.

#### Scenario: EXIT removes temporary data
- **WHEN** `--fly` runs with commands that persist data followed by `EXIT`
- **THEN** the temporary directory no longer exists after the application terminates

### Requirement: Fly mode in both input modes
`--fly` SHALL be usable interactively (`--fly` alone) and with a command file (`--fly <file>`).

#### Scenario: Fly with command file
- **WHEN** the application runs with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are executed using the temporary directory, which is removed at the end
