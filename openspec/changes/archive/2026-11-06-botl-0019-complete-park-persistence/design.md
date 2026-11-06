# Design

## Context

See proposal.md – Why. Users are persisted by `UserFileStore` (one `;` line per user) through `UserRepository` (save-before-update). Groups (members by DNI), tickets (`IndividualTicket` with `User`; `GroupTicket` with `Group`, group id and composition) and attractions live only in memory. `ParkController.start(UserRepository, LocalDate)` builds services; `Main` reads `park.users.file`.

## Goals / Non-Goals

**Goals:**
- Same repositories for normal and `--fly` modes; only the data directory changes.
- Faithful reconstruction of links and fiscal records.

**Non-Goals:**
- Atomic multi-file transactions; concurrent executions sharing files.

## Decisions

- **Data directory**: `DataDirectory` (persistence package) resolves file paths. `DataDirectory.permanent(Path)` (normal mode, `park.data.dir` property defaulting to the working directory, keeping `users.txt` where it was) and `DataDirectory.temporary(Path base, LocalDateTime now)` creating `base/yyyyMMdd_HHmmss` (suffix `_N` if it already exists). It is `AutoCloseable`; closing a temporary directory deletes it recursively. `Main` only selects the mode and uses try-with-resources, so cleanup happens on `EXIT`, file end and stdin end.
- **Files and formats** (`;`-separated, fields never contain `;` because commands are `;`-separated):
  - `groups.txt`: `id;name;dni1,dni2,...`
  - `attractions.txt`: `id;name;capacity`
  - `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;dni;firstName;surnames;email` and `GROUP;id;date;basePrice;discount;attachedGroupId|<empty>;groupId` followed by one `MEMBER;dni;firstName;surnames;email` line per constituent.
- **Stores**: `TextFile` (read/write lines, UTF-8, `PersistenceException`), `GroupFileStore`, `AttractionFileStore`, `TicketFileStore` (load takes lookups `dni → User` and `id → Group`; a missing user is rebuilt from the snapshot as a detached `User`). No Java serialization.
- **Repositories**: `GroupRepository`, `AttractionRepository`, `TicketRepository` get optional stores, load on construction and expose `save()`; services call it after each successful mutation (create/add/remove/delete/detach). `ParkController.deleteUser` removes the user from groups through `GroupService.removeFromAllGroups`, which saves.
- **Reconstruction order** in `ParkController.start(Path dataDir, LocalDate)`: users → groups → attractions → tickets. Existing `start(UserRepository, LocalDate)` remains for in-memory use.
- **`GroupTicket` reconstruction constructor** taking explicit discount, group (nullable) and group id.

## Risks / Trade-offs

- [Group/ticket/attraction saves happen after the in-memory change] → a failed write is reported as an error; the file is rewritten on the next successful save. Users keep their stronger save-before-update guarantee.
- [Temporary dir timestamp collisions within one second] → `_N` suffix.
