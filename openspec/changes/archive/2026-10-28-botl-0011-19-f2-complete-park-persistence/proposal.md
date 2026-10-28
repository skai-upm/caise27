# Proposal

## Why

Milestone F2-C08 requires the park state — users, groups and their members, individual tickets and their user, group tickets with their composition and (while it exists) their group, and attractions — to be stored in text files and reconstructed on the next execution, and adds the `--fly` temporary persistence mode that uses the same repositories pointed at a timestamped temporary directory deleted at the end of the run. Nothing is persisted today, so every execution starts empty.

## What Changes

- Add a persistence layer, separate from the domain and the controller, with one text-file repository per element type (users, groups, tickets, attractions); no Java serialization, databases, frameworks or external libraries.
- Load the state at start-up and save it after every processed command so a later execution reconstructs it; attraction occupants are not persisted (attractions start empty).
- Historical tickets keep the minimum data of their user (DNI, e-mail) and group tickets their composition, so they are reconstructed even when the user or group no longer exists (group reference becomes null).
- Normal mode stores files in `data/` under the working directory.
- Add `--fly`: as first argument (optionally followed by a command file) it creates `tmp/<yyyyMMdd_HHmmss>/`, configures all repositories with paths inside it, and deletes it when execution ends via EXIT or end of input. Argument handling without `--fly` stays exactly as before.
- Commit with the exact message `[F2-C08] Extend park persistence`.

## Capabilities

### New Capabilities
- `park-persistence`: What park state is persisted, how it is reconstructed, and what is not persisted.

### Modified Capabilities
- `application-execution`: adds the `--fly` temporary persistence execution mode.

## Impact

- New `persistence` package, `App` argument handling and lifecycle, controller restore/listing operations, `.gitignore` entries for `data/` and `tmp/`, tests.
- **Flagged ambiguities / ordering conflicts:**
  - User persistence (F1-C04) and the full user model (F1-C01) come later in this batch. A user repository is added now because groups and tickets cannot be reconstructed without users; it stores the attributes users currently have (DNI, e-mail) and F1-C04 will extend it.
  - The location of permanent data files is not specified; `data/` under the working directory is used.
  - When to write is not specified; state is saved after every command so an abrupt end loses at most the command in progress.
  - Two `--fly` runs starting in the same second would collide on the timestamp; a numeric suffix is appended in that case.
  - Corrupt or unreadable lines in data files are reported with an error message and skipped.
