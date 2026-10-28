# Design

## Context

All state lives in the `ParkController` singleton (users by e-mail, groups, top-level tickets, attractions). `GroupTicket` is only built via `purchase` (computes prices) and `registerTicket` is package-private. `App.run(args, stdin, out)` handles interactive/file modes. Integration tests call `App.run` directly, so persistence must not write into the repository's working directory during tests.

## Goals / Non-Goals

**Goals:** text-file repositories per element, load at start/save after each command, `--fly` mode with cleanup.

**Non-Goals:** persisting attraction occupants, date or price; full user attributes (F1-C01/F1-C04).

## Decisions

- **Package `persistence`**: `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, each constructed with a `Path` to its file (`users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`) and offering `load(...)`/`save(...)`. `ParkStorage` builds the four repositories from a directory and transfers state between files and the controller. The controller has no file I/O; repositories have no business rules. Same classes for both modes — only the directory differs.
- **Formats** (`;`-separated, values cannot contain `;` because commands are split on it; parsed with `split`, no regex):
  - users: `dni;email`
  - groups: `id;name;email1;email2;...`
  - tickets: `I;id;date;price;dni;email` for individual tickets; `G;id;date;groupId|-` followed by its composition lines `C;id;price;dni;email`
  - attractions: `id;name;capacity`
- **User resolution on load**: a ticket's user is the active user with the same e-mail and DNI; otherwise a detached `User(dni, email)` is created (historical record of a deleted user). Groups resolve members by e-mail among active users.
- **Controller additions**: public `reset()`, read-only `getUsers()/getGroups()/getTickets()/getAttractions()`, `restoreGroupMember`, `restoreTicket(Ticket)` (identifier check, no date/price rules) and `restoreAttraction`. `GroupTicket.restore(id, date, group, composition)` rebuilds without recomputing prices.
- **Lifecycle in `App.run(args, stdin, out, workDir)`**: parse `--fly`; choose `workDir/data` or a new `workDir/tmp/<yyyyMMdd_HHmmss>[_n]`; `reset()` controller, load, run the processor with an after-command hook that saves, delete the fly directory in a `finally` block. `main` passes the current directory.
- **`.gitignore`**: add `data/` and `tmp/` (runtime files).

## Risks / Trade-offs

- [Saving after every command rewrites four small files] → acceptable for course-sized data; simple and robust.
- [Malformed lines] → reported with `ERROR:` and skipped, the rest loads.
