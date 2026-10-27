# Proposal

## Why

Milestone F2-C08: groups, tickets and attractions are lost when the application ends. The park state and its relationships must be stored in text files and rebuilt at start-up, and a temporary `--fly` mode must allow isolated, repeatable runs that leave no data behind.

## What Changes

- Persist, in text files and without Java serialization: groups and their members; individual tickets and their user; group tickets with their composition and, while it exists, the purchasing group; attractions (without occupants — every execution starts with empty attractions). Users keep their Delivery 1 file and format.
- Tickets keep the minimum user data (DNI, first name, surnames, email) so historical tickets of deleted users and historical group tickets of deleted groups are rebuilt with identifier, date, price and composition.
- All repositories take their file locations from one data-directory configuration: the working directory normally, or `tmp/<yyyyMMdd_HHmmss>/` with `--fly`; there is no fly-specific repository.
- Support `--fly` as the first argument, optionally followed by a command file: the temporary directory is created at start-up and deleted with all its files when execution ends (EXIT or end of input/file). Without `--fly`, argument handling is unchanged.
- Commit with the exact message `[F2-C08] Extend park persistence`.

## Capabilities

### New Capabilities
- `park-persistence`: Persistence of groups, tickets and attractions and their relationships, and the temporary `--fly` mode.

### Modified Capabilities
- `execution-modes`: launch forms extended with `--fly` and `--fly <file>`.

## Impact

- New `persistence/DataPaths`, `GroupFileStore`, `TicketFileStore`, `AttractionFileStore`, `TextFiles`; repositories accept an optional store and `save()`; `ParkController.start(DataPaths)`; `Main` handles `--fly`.
- Ambiguities flagged: normal-mode files live in the working directory beside `users.txt`; if a timestamp directory already exists (two runs in the same second) a numeric suffix is appended; the `tmp/` parent is removed too when left empty; occupants are discarded at start-up as required; system date/price are not persisted (F2-C01).
