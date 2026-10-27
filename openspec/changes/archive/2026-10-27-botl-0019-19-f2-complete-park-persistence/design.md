# Design

## Context

Only users are persisted (`UserFileStorage`, `data/users.txt`, saved on each repository change). Groups, tickets and attractions live in memory with object references (group → users, ticket → user, group ticket → composition + group). The source leaves the file organisation to the students but forbids serialization, databases, frameworks and external libraries, and requires `--fly` to reuse the same repositories with different paths.

## Goals / Non-Goals

**Goals:**
- Reconstruct every persisted element and relationship, including historical tickets of deleted users/groups; `--fly` isolation and cleanup.

**Non-Goals:**
- Persisting system date/price (per-execution by definition) or attraction occupants (explicitly excluded).

## Decisions

- **One text file per repository** inside the data directory, `;`-separated, one element per line:
  - `users.txt`: unchanged (`firstName;surnames;dni;email;password;phone;card`).
  - `groups.txt`: `id;name;dni1;dni2;...` — members by DNI (immutable identifier).
  - `attractions.txt`: `id;name;capacity`.
  - `tickets.txt`: `INDIVIDUAL;id;date;price;groupTicketId;dni;firstName;surnames;email` and `GROUP;id;date;groupId` (empty `groupTicketId`/`groupId` when absent).
- **User snapshot in ticket lines:** on load, an individual ticket links to the active user with that DNI; if none exists (deleted user), a detached `User` is rebuilt from the snapshot (password/phone/card empty) and shared by all tickets with that DNI. This is the "minimum information needed" for historical tickets (assumption, flagged: identification data, not credentials or card).
- **Load order:** users → groups (resolve DNIs) → tickets (individual first, then group tickets assembled from individual tickets carrying their id; group by id) → attractions. Malformed lines are skipped and reported as a warning.
- **Saving:** repositories keep saving on add/delete, and `CommandProcessor` calls `ParkController.persistAll()` after every command so object mutations (group membership, cleared group references, user updates) are written too. Write errors are reported as `ERROR:` without stopping.
- **`ParkController.initialize(users, groups, tickets, attractions, today)`**: per-execution state; older overloads keep in-memory repositories for tests.
- **`--fly`:** `App` checks `args[0].equals("--fly")`; data directory = `tmp/<LocalDateTime formatted yyyyMMdd_HHmmss>` (suffix `_1`, `_2`… if it already exists, flagged); remaining args follow the Delivery 1 rules. A `finally` block deletes the directory tree after the run (EXIT or end of input), and removes `tmp/` if it is left empty. Same repository/storage classes, only the path differs.
- **Mandatory commit:** `[F2-C08] Extend park persistence` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Rewriting all files after each command] → Small data volumes; guarantees consistency of mutable relationships.
- [Abnormal termination in `--fly` (kill)] → The directory cannot be cleaned; the source only requires cleanup on EXIT/normal end of file.
