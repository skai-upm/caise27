# Design

## Context

All park state lives in the `ParkController` singleton (groups, tickets in one identifier map, attractions). Users are referenced by email and not yet persisted. Delivery restrictions: text files, standard library, no serialization, no regex, and separation of responsibilities (the controller must not do file I/O).

## Goals / Non-Goals

**Goals:** a faithful text round-trip of groups, tickets and attractions, fiscal-record safety, and `--fly` handled purely by path configuration.
**Non-Goals:** user persistence, concurrency or multi-process safety, data migration.

## Decisions

- **Port in the controller layer**: `ParkStorage` interface (`ParkState load()`, `void save(ParkState)`), where `ParkState` is a record of the groups, tickets (top-level tickets only: individual tickets bought alone and group tickets) and attractions. `ParkController` holds a `ParkStorage` (default: in-memory no-op), calls `save` after every successful mutation, and has `restore(ParkState)` to rebuild its maps, including the composed tickets of each group ticket. The controller never touches files.
- **Adapters** in `es.upm.park.persistence`: `StoragePaths` (data directory → file paths), `GroupRepository` (`groups.txt`), `TicketRepository` (`tickets.txt`), `AttractionRepository` (`attractions.txt`), and `TextParkStorage` composing them. The same classes serve normal and `--fly` mode. Only the directory differs.
- **Line formats**: `;`-separated, matching the command syntax. Command arguments are split on `;`, so stored values never contain it. Text is UTF-8 and parsed with `split(";", -1)` and no regex.
  - group: `id;name;email1;email2;...`
  - individual ticket: `I;id;date;price;email`
  - group ticket: `G;id;date;groupId-or-empty;ticketId1;email1;price1;ticketId2;email2;price2;...`
  - attraction: `id;name;capacity`
- **Fiscal safety**: tickets store the user email and price directly, so they never depend on the user still existing. A group ticket stores its group id only while the reference is set. `deleteGroup` detaches the reference and the change is saved immediately, so a recreated group with the same id is never linked.
- **Writes** go to `<file>.tmp` and are then moved over the target with `ATOMIC_MOVE` (falling back to `REPLACE_EXISTING`), so a crash cannot leave a half-written file.
- **Loading**: a missing file means an empty collection. Malformed lines are reported on stderr and skipped. Group ticket references are resolved by id against the loaded groups.
- **`App`**: parses `--fly` and picks the data directory (`data/`, or `tmp/<yyyyMMdd_HHmmss>/`, adding a `_n` suffix if it already exists). It wires `TextParkStorage`, restores the state, runs the command loop, and in `finally` deletes the temporary directory recursively. `App.run` receives a base directory so tests never write into the project.

## Risks / Trade-offs

- [Saving the whole state after every mutation is O(n) per command] → The data volumes in this course are tiny, and full rewrites keep the files consistent.
- [A temp directory is not deleted if the JVM is killed] → The source requires cleanup only for normal termination.
