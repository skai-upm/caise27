# Proposal

## Why

Milestone F2-C08 (Delivery 2, step 8) requires the park to survive restarts. That covers groups and their users, individual tickets and their user, group tickets with their composition and (while it exists) their group, and attractions. Historical tickets must stay reconstructable as fiscal records even after their user or group is deleted. Attractions start empty on every execution. A temporary `--fly` mode must run against a fresh timestamped directory, configured purely through repository paths, and that directory is deleted at the end of the run (source §13, §13.1). Today only users are persisted, and every other element is lost when the application ends.

## What Changes

- Persist groups (identifier, name, member DNIs), individual tickets (identifier, date, price, and holder DNI, email, first name and surnames), group tickets (identifier, date, purchasing group if it still exists, composition ticket identifiers) and attractions (identifier, name, capacity) in `;`-separated text files next to `users.txt`. No Java serialization, databases or libraries are used.
- Load them at start-up after users, re-linking members, holders and purchasing groups by identifier. A historical ticket whose user no longer exists keeps its stored holder data. Malformed lines are ignored and reported with the same warning as the users file.
- Save the park state after every successful command. Attraction occupants are never stored, so attractions start empty in every execution.
- Add `--fly`. When it is the first argument, all repositories use files inside a new directory `tmp/<yyyyMMdd_HHmmss>/` (a numeric suffix is added if the name exists). That directory and its files are deleted when execution ends, whether through `EXIT` or end of input, interactively or with a command file. `--fly` may be followed by a command file path. Without `--fly`, argument handling is unchanged.

In scope: persistence of the Delivery 2 elements, historical reconstruction and the `--fly` mode.

Out of scope: persisting the system date or price (both reset per execution by specification) and attraction occupancy.

## Capabilities

### New Capabilities
- `park-persistence`: Storage and reconstruction of groups, tickets (with historical fiscal data) and attractions between executions, plus the temporary `--fly` persistence mode.

### Modified Capabilities
- `command-interface`: the command-file requirement now applies to the first argument that is not `--fly`, so that `--fly input.txt` processes `input.txt`.

## Impact

- New `persistence.ParkFileStore` and `app`-level `StorageLocation` (data directory for normal or `--fly` mode). `ParkController` loads and saves through the store. `CommandProcessor` persists after successful commands. `App` parses `--fly` and cleans up the temporary directory. `tmp/` is git-ignored. Tests.
