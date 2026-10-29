# Design

## Context

All state lives in the `ParkController` singleton (users, groups, tickets, attractions) with package-private `reset()`. `App.main` reads stdin or `args[0]`. No persistence exists yet.

## Goals / Non-Goals

**Goals:** text-file repositories configured by directory; load at start, save after each command; `--fly`.

**Non-Goals:** full user fields (F1-C04), system date/price persistence, migration of formats.

## Decisions

- **Layering**: `TextFileRepository` (abstract, generic) handles reading/writing a list of lines in one file (`Files.readAllLines`/`write`, UTF-8; missing file = empty). Concrete repositories — `UserRepository` (`users.txt`), `GroupRepository` (`groups.txt`), `TicketRepository` (`tickets.txt`), `AttractionRepository` (`attractions.txt`) — convert domain objects to/from `;`-separated lines using `split(";", -1)`. `ParkStorage` holds the four repositories built from one `Path` directory and offers `load(ParkController)` / `save(ParkController)`. Domain classes contain no I/O.
- **Formats**:
  - users: `email;dni`
  - groups: `GROUP;id;name` and `MEMBER;groupId;email`
  - tickets: `INDIVIDUAL;id;date;price;email;dni`, `GROUP;id;date;groupId|-`, `COMPONENT;groupTicketId;id;price;email;dni` (components after their group ticket; group ticket price = sum of components)
  - attractions: `id;name;capacity`
  Tickets embed email and DNI so they can be rebuilt even if the user is not in the user file.
- **Reconstruction** uses package-private `ParkController.restore...` methods that bypass "no past date" rules (historical tickets must load) but keep identifier uniqueness. Load order: users → groups → attractions → tickets. Malformed lines are skipped with a warning line.
- **Lifecycle** (`App.run(args, baseDir, in, out)`, `main` delegates with the working directory): parse `--fly`; choose data dir (`data/` or a new `tmp/<yyyyMMdd_HHmmss>[_n]/`); `park.reset()`; `storage.load`; attach storage to the processor, which saves after every processed command; in `finally`, delete the fly directory recursively.
- **Git**: add `/data/` and `/tmp/` to `.gitignore`.

## Risks / Trade-offs

- [Save after every command costs I/O] → state is small; it removes any dependency on how the execution ends.
- [Restore bypasses date validation] → only reachable from the storage layer in the same package.
