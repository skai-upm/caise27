# Proposal

## Why

Milestone F2-C08 requires the park state — groups and their users, individual tickets and their user, group tickets with their composition and (while it exists) their group, and attractions — to survive the end of an execution and be reconstructed at the next start, from text files, keeping persistence separate from the domain. It also adds the `--fly` mode: the same repositories pointed at a fresh timestamped temporary directory that is deleted when the execution ends.

## What Changes

- A persistence layer of text-file repositories (users, groups, tickets, attractions), one file each, configured only by a data directory path. No Java serialization, databases or external libraries.
- Normal mode stores data under `data/` (relative to the working directory); state is loaded at start and saved after every command, so it is available to the next execution.
- Attractions are persisted without their occupants; every attraction starts empty in a new execution. System date and price are not persisted (they reset per execution, as already specified).
- Historical tickets keep the user data needed to rebuild them; group tickets keep their composition and their group reference only while the group exists.
- `--fly` as first argument (optionally followed by a command file): creates `tmp/<yyyyMMdd_HHmmss>/`, all repositories use files inside it, and the directory is deleted at the end of the execution (EXIT or normal end of the command file / input). Without `--fly`, argument handling is unchanged.
- `data/` and `tmp/` are ignored by Git.

## Capabilities

### New Capabilities
- `persistence`: what park state is preserved between executions, how it is reconstructed, and the `--fly` temporary execution mode.

### Modified Capabilities
- None (the `--fly` execution forms are specified in `persistence`; existing input-source behavior is unchanged).

## Impact

- New classes `ParkStorage` (orchestrates the repositories) and `TextFileRepository`-based repositories; `App` argument handling and lifecycle; `ParkController` load/restore hooks; `.gitignore`; tests use temporary directories only.

## Ambiguities and unmet prerequisites (flagged)

- **Users:** user persistence is the Delivery 1 milestone F1-C04, applied later in this batch. Reconstructing groups and tickets is impossible without their users, so this change persists the user data that currently exists (email and DNI). F1-C04 extends this repository with the remaining user fields.
- The data directory location is not specified; `data/` under the working directory is used (`tmp/` is given by the source for `--fly`).
- File formats are the student's decision; semicolon-separated lines (fields cannot contain `;` because commands are split on it), read with `split`.
- Saving after every command (rather than only at exit) is a design choice for robustness; it also guarantees state is saved when a command file ends without EXIT.
- If two `--fly` executions start in the same second, a numeric suffix is added to keep directories independent.
- Malformed lines in data files are skipped with a warning instead of aborting the start-up.
