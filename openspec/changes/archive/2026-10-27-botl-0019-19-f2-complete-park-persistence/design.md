# Design

## Context

Only users are persisted (`UserFileStore` → `users.txt` in the working directory, loaded by `UserRepository`). Groups/tickets/attractions repositories are in-memory and recreated by `ParkController.start`.

## Goals / Non-Goals

**Goals:** text persistence of all park elements with relationships; one path configuration for normal and `--fly` modes.
**Non-Goals:** persisting date/price or attraction occupants.

## Decisions

- **`DataPaths(Path dir)`** names the four files (`users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`) inside `dir`; normal mode uses `.`, `--fly` uses `tmp/<timestamp>`. Repositories receive their store built from these paths — the only difference between modes.
- **Stores** (persistence package) mirror `UserFileStore`: `load(...)` / `save(collection)`, missing file → empty, malformed lines skipped, `;`-separated fields:
  - `groups.txt`: `id;name;dni,dni,...` (members resolved against loaded users).
  - `tickets.txt`: `I;id;date;basePrice;dni;firstName;surnames;email` and `G;id;date;basePrice;groupId;memberCount` followed by `memberCount` lines `M;dni;firstName;surnames;email`. Users are resolved by DNI among registered users; otherwise a detached snapshot `User` (blank password/phone/card) is built — the ticket keeps its fiscal data. Group tickets are rebuilt through the normal `GroupTicket` constructor, so derived ids, discount and price are identical.
  - `attractions.txt`: `id;name;capacity`.
- **Load order**: users → groups → tickets → attractions, inside `ParkController.start(DataPaths)`.
- **Saving**: repositories save after their own `add/remove`; changes made through model objects (group membership, clearing a ticket's group) call the repository's `save()` explicitly from the controller/commands.
- **`--fly`** in `Main`: create `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if taken), run, then in `finally` delete the directory tree and `tmp/` if empty — covers EXIT, end of input and a missing command file.
- `Main.run(args, in, out, err, Path baseDir)` resolves both modes relative to `baseDir` (`.` in production, a temp dir in tests).
