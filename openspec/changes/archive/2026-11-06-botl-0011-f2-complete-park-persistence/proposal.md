# Proposal

## Why

Milestone F2-C08 requires the park state introduced in Delivery 2 (groups, individual tickets, group tickets and attractions, with their relationships) to survive application restarts in text files. Historical tickets must stay reconstructable as fiscal records even after their user or group is deleted. It also adds a temporary `--fly` mode for isolated, repeatable runs that never touch normal data. Today the whole park state is lost at every exit.

## What Changes

- Add text-file persistence for groups with their members, individual tickets with their user, group tickets with their composed individual tickets and their group reference (while the group exists), and attractions. Attraction occupants are not persisted, so every execution starts with empty attractions.
- Save the persistent state whenever it changes, and reconstruct it when the application starts. Only text files and the Java standard library are used: no object serialization, databases or libraries.
- Historical tickets keep the data needed to rebuild them (identifier, date, price, user email, composition) even when their user or group no longer exists. A group ticket whose group was deleted is restored without a group reference.
- Add the `--fly` execution mode. When `--fly` is the first argument, the application creates `tmp/<yyyyMMdd_HHmmss>/` and points every repository at it, and deletes the directory with all its files when the execution ends (EXIT or end of input). It can be followed by an optional command file path. Without `--fly`, argument handling is unchanged and data is kept in `data/`.
- Ignore `data/` and `tmp/` in Git.

### In scope
- Persistence of groups, tickets and attractions, `--fly`, and the repository path configuration.

### Out of scope
- User persistence (F1-C04, processed later in this batch). It will use the same repository path configuration, so `--fly` covers users once they are persisted.
- Persisting the system date and base price. The price resets to 40.00 on every execution by rule, and the date starts as the local date.

## Capabilities

### New Capabilities
- `park-persistence`: storage of the park state in text files between executions, reconstruction at startup, retention of fiscal records, and the temporary `--fly` storage location.

### Modified Capabilities
- `application-execution`: file mode now applies to the first argument that is not `--fly`. A new requirement adds the `--fly` execution forms.

## Impact

- New package `es.upm.park.persistence` (path configuration, group, ticket and attraction repositories, and the text storage facade).
- `ParkController`: a storage port, a save after each mutation, and restore. Model factories for restoring group tickets.
- `App`: argument parsing for `--fly`, storage wiring, and cleanup of the temporary directory.
- `.gitignore`: `data/`, `tmp/`. New tests for round-trips and `--fly`.
