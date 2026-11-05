# Design

## Context

Users persist via `UserFileStore` and `UserRepository` (saved after each change, `users.txt` in the working directory). Groups, tickets and attractions live in in-memory repositories owned by the `ParkController` singleton. `App` builds the users repository and the processor; `start(args…)` treats `args[0]` as the command file.

## Goals / Non-Goals

**Goals:**
- Full park round-trip with no change to command semantics.
- `--fly` implemented purely by choosing a different data directory.

**Non-Goals:**
- Incremental or transactional saves. Whole-file rewrites are acceptable at this scale.

## Decisions

- **`persistence.ParkFileStore(Path dataDir)`**: one class for the three new files, separate from the model and `App` (§13 separation of responsibilities):
  - `groups.txt`: `id;name;dni1,dni2,…`
  - `tickets.txt`, one of:
    - `I;id;date;price;dni;email;firstName;surnames`
    - `G;id;date;groupIdOrEmpty;componentId1,componentId2,…`
    Individual lines are written before group lines, so components resolve.
  - `attractions.txt`: `id;name;capacity`
  - `load(ParkController)` and `save(ParkController)`.
- **Holder reconstruction**: an active user with the stored DNI is linked if one exists. Otherwise a detached `User(firstName, surnames, dni, email, "", "", "")` represents the historical holder. It is never added to the user repository, so it stays inactive, and only the fields needed for the fiscal record are kept.
- **Group ticket price** is recomputed as the sum of its components (as on creation), so it needs no separate column.
- **`ParkController.startExecution(UserRepository, ParkFileStore)`** loads, and **`persist()`** saves when a store is configured. `CommandProcessor.process` calls `park.persist()` after any response starting with `OK`. This one hook covers every mutating command, and users stay self-persisting.
- **`StorageLocation`** resolves the data directory from the arguments: `--fly` gives `tmp/<yyyyMMdd_HHmmss>` (created, with a numeric suffix on collision); otherwise the working directory. `App.main` strips `--fly` before passing the remaining arguments to the unchanged `start`, and deletes the fly directory in a `finally` block.
- **Warnings**: ignored lines from all park files are summed and reported like the users file.

## Risks / Trade-offs

- [A crash (kill) during fly mode leaves the tmp directory behind] → Normal termination paths (EXIT and end of input) are covered, as specified. `tmp/` is git-ignored.
- [Detached historical holders have blank password/phone/card] → Those fields are not part of the fiscal record.
