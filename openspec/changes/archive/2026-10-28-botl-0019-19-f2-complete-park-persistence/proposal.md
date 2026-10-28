# Proposal

## Why

Milestone F2-C08: groups, tickets and attractions must survive between runs like users do, with their relationships reconstructed on startup, and a temporary `--fly` mode must allow isolated, repeatable executions that never touch the permanent data.

## What Changes

- Persist, in text files and with separate persistence classes per element (no Java serialization, databases, frameworks or external libraries):
  - groups and their members;
  - individual tickets with their user;
  - group tickets with their composition and, while the group exists, the purchasing group;
  - attractions (without occupants — every attraction starts empty).
- Preserve historical tickets of deleted users/groups: tickets store the minimum user data (DNI, name, surnames, email) needed to rebuild them; detached group tickets are stored without a group.
- Configure all persistence classes from one data location (directory). Normal mode uses the working directory (`users.txt` stays where it was, plus `groups.txt`, `tickets.txt`, `attractions.txt`).
- Add `--fly` as optional first argument (optionally followed by a command file): create `tmp/<yyyyMMdd_HHmmss>/`, point all persistence classes to it, and delete it when execution ends (EXIT, end of file, or end of standard input). Without `--fly` argument handling is unchanged.
- Commit with the mandatory message `[F2-C08] Extend park persistence`.

## Capabilities

### New Capabilities
- `park-persistence`: Persistence of groups, tickets and attractions with their relationships, reconstruction at startup, and the temporary `--fly` persistence mode.

### Modified Capabilities
- `execution-modes`: the accepted argument forms now include `--fly` and `--fly <file>`.

## Impact

- New `persistence.DataLocation`, `GroupFileStorage`, `TicketFileStorage`, `AttractionFileStorage`; `ParkController` loads/saves them; `CommandProcessor` saves the park state after successful state-changing commands; `App` parses `--fly` and cleans up; `.gitignore` gains `tmp/`, `groups.txt`, `tickets.txt`, `attractions.txt`.
- Ambiguities flagged:
  - File layout/format is the student's choice (see design); lines use `;` like `users.txt`.
  - If the timestamp directory already exists (two runs in the same second), a numeric suffix is added.
  - References that cannot be resolved on load (e.g. a group member DNI that no longer exists) are skipped with a warning on standard error.
  - Saving the park state is done after a successful state-changing command; if writing fails, an `ERROR:` line is printed (the in-memory change has already been applied).
