# Design

## Context

`UserRepository` persists through `UserStorage`/`UserFileStorage` (`users.txt`). Groups, tickets and attractions live in in-memory repositories created by `Park.start`. Relationships: groups → users; individual tickets → user; group tickets → composition and (nullable) group.

## Goals / Non-Goals

**Goals:** same repositories in normal and `--fly` mode, only the directory differs; historical records survive deletions. **Non-Goals:** concurrency, incremental writes, persisting occupants.

## Decisions

- **One data directory** (`.` normally, `tmp/<stamp>` with `--fly`) passed to `Park.start(Path)`, which builds `UserFileStorage(users.txt)`, `GroupFileStorage(groups.txt)`, `TicketFileStorage(tickets.txt)`, `AttractionFileStorage(attractions.txt)` and loads them in dependency order: users → groups (members by DNI) → tickets (users by DNI, groups by identifier) → attractions. `Park.start(UserRepository)` (in-memory for the rest) remains for tests.
- **Storage interfaces with `none()` defaults**, injected into repositories; repositories save the full file after each mutation (`add`/`remove`/`update`), as users already do. Services call `update` after mutating objects (group membership, group detachment, user deletion → tickets re-saved so user snapshots are current).
- **Formats** (`;`-separated, no field can contain `;` because commands are split on it):
  - `groups.txt`: `id;name;dni1,dni2,...`
  - `tickets.txt`: `I;id;date;price;dni;firstName;surnames;email` (standalone individual), `G;id;date;groupId;attached(1|0)` followed by its composition lines `C;groupTicketId;id;date;price;dni;firstName;surnames;email`.
  - `attractions.txt`: `id;name;capacity`.
- **User resolution for tickets**: DNI found among registered users → that user; otherwise a "former user" `User` is rebuilt from the snapshot (password/phone/card empty), preserving the fiscal record.
- **Group resolution**: only when `attached=1` and the group exists; `attached=0` keeps the group reference null even if a new group reuses the identifier.
- **`--fly` lifecycle in `App`**: parse `--fly`, create the directory, run, then delete it recursively in a `finally` block (covers EXIT, end of input and end of file; an unreadable command file also cleans up).

## Risks / Trade-offs

- [Abnormal termination (kill) leaves the temp directory] → acceptable; the source requires cleanup at normal ends.
- [Snapshot email of deleted users may differ from older values] → the snapshot is the latest data at deletion time.
