# Proposal

## Why

Milestone F2-C08 (Delivery 2, step 8) requires the whole park state — groups, tickets and attractions, with their relationships — to survive between runs and be reconstructed at start-up, including historical (fiscal) tickets of users or groups that no longer exist. It also adds the `--fly` temporary persistence mode for isolated, repeatable executions that never touch the permanent data. Today only users are persisted.

## What Changes

- Persist groups (with their users), individual tickets (with their user), group tickets (with their constituent tickets and, while the group exists, the purchasing group) and attractions in text files; reconstruct them at start-up. Attraction occupants are not persisted: every attraction starts empty.
- Keep enough user data in ticket records (DNI, first name, surnames, email) to reconstruct historical tickets of deleted users; keep the group identifier of historical group tickets whose group was deleted.
- Add `--fly` as optional first argument (optionally followed by a command file): create `tmp/<yyyyMMdd_HHmmss>/`, point every repository at files inside it, and delete it when execution ends (after `EXIT`, at the end of a command file, or at the end of standard input). Without `--fly`, behavior and file locations are unchanged.
- No Java serialization, databases, frameworks or external libraries; file I/O stays in the persistence layer, separate from models and the main class.

### In scope
- Source sections 13 and 13.1.

### Out of scope
- UML model (F2-C09), Delivery 2 integration (F2-C10).

## Capabilities

### New Capabilities
- `park-persistence`: persistence and reconstruction of groups, tickets and attractions, and the `--fly` temporary mode.

### Modified Capabilities
- `command-interface`: argument forms now include `--fly` with or without a command file.

## Impact

- New `GroupFileStore`, `TicketFileStore`, `AttractionFileStore`, `TextFile`, `DataDirectory` in `es.upm.park.persistence`; repositories optionally backed by stores; services save after mutations; `ParkController.start(Path dataDir, LocalDate)`; `Main` argument handling; `.gitignore` for the new data files and `tmp/`.
