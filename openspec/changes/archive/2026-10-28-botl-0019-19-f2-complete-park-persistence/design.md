# Design

## Context

`users.txt` is written by `UserFileStorage` via `UserService` (save-then-commit). Groups, tickets and attractions live only in repositories owned by `ParkController`. `App.execute(args, stdin, out, err, usersFile)`. See proposal.md.

## Goals / Non-Goals

**Goals:** full reconstruction of the park state; one configurable data location; `--fly`.
**Non-Goals:** persisting occupants, date or price.

## Decisions

- **`persistence.DataLocation(Path directory)`** with `users()`, `groups()`, `tickets()`, `attractions()` file paths. Normal mode: `.`; fly mode: `tmp/<timestamp>`. Same storage classes in both modes.
- **File formats** (UTF-8, `;`-separated; command parsing guarantees values never contain `;`):
  - `groups.txt`: `id;name;dni1,dni2,...` (DNIs never contain `,`).
  - `attractions.txt`: `id;name;capacity`.
  - `tickets.txt`, one record per ticket: `I;id;date;basePrice;dni;firstName;surnames;email` for individual tickets; `G;id;date;basePrice;groupId` (empty group id when detached) followed by its members as `M;id;dni;firstName;surnames;email`.
- **Reconstruction** (`ParkController.initialize(DataLocation)`): users → groups (unknown member DNIs skipped with warning) → attractions → tickets (user by DNI, or a historical `User` rebuilt from the stored fields; group by id, else none). Group tickets are rebuilt with the `GroupTicket` constructor from the stored base price and members, which reproduces the derived identifiers and discount.
- **Saving**: `ParkController.saveState()` writes groups, tickets and attractions; `CommandProcessor` calls it after every successful state-changing command (`USER *`, `GROUP *`, `TICKET *`, `GROUPTICKET *`, `ATTRACTION CREATE`). Users keep their save-before-commit path.
- **Storage classes create the parent directory on save**, so a fresh data directory works.
- **`App`**: `execute(args, stdin, out, err, dataDir, flyRoot)`; `--fly` → `flyRoot/<yyyyMMdd_HHmmss>[_n]`, created at start and recursively deleted in a `finally` block; remaining argument (if any) is the command file.

## Risks / Trade-offs

- [Saving after the in-memory change] → failure reported as `ERROR:`; files are rewritten completely on the next successful save.
- [Historical users rebuilt without password/phone/card] → those fields are not needed for fiscal records.
