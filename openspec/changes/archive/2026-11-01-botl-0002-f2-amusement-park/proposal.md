# Proposal

## Why

F1 delivered user management, file persistence, and shared interactive/file command execution. Delivery 2 (F2) turns the application into an amusement-park model: a simulated system date, user groups, individual and group tickets with pricing rules, attractions with capacity-controlled access, a PlantUML model of the implemented design, and an isolated `--fly` execution mode. These features interact (tickets block deletions, group tickets lock groups, attractions admit ticket holders), so they are delivered as one coherent change that keeps every F1 behaviour working.

## What Changes

- **System date and base price**: one runtime system date (initialised to the OS date, changed with `SYSTEM DATE;yyyy-MM-dd`) used by every date-dependent rule; a runtime base ticket price (initialised to `40.00`, changed with `SYSTEM PRICE;60.00`). Neither setting is persisted.
- **Groups**: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`; groups reference existing users (no copies) and are persisted.
- **Individual tickets**: `TICKET CREATE` (reports `OK TICKET CREATED -> <Precio>`) and `TICKET DELETE` (future tickets only); tickets keep their purchase price, persist, and are retained as history.
- **Group tickets**: `GROUPTICKET CREATE` (one constituent ticket per current member, size-bracket discounts, reports `OK GROUP TICKET CREATED -> <Precio>`) and `GROUPTICKET DELETE` (future only, all-or-nothing); a common ticket abstraction for both types.
- **Attractions**: `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`; capacity-bounded, atomic group entry; definitions persist, occupancy is runtime-only.
- **User deletion rule (modified)**: `USER DELETE` is rejected while the user has a ticket dated on or after the system date.
- **Group locking**: a group with a group ticket dated on or after the system date cannot change membership or be deleted.
- **UML**: `uml/model.puml` generated from the implemented code.
- **`--fly` mode**: all repositories use a per-run `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted on termination; works interactively and with a command file.
- **Command execution (modified)**: new commands registered in the shared processor and listed by `HELP`; the command-line accepts `--fly` before the optional command-file path.

Out of scope: F3 features (SpeedPass, Special Pass, invoice builder, BCrypt, attraction categories), mandated Singleton, web/API interfaces, databases.

## Capabilities

### New Capabilities
- `system-date`: Runtime system date and its `SYSTEM DATE` command, used by all date-dependent rules.
- `group-management`: Groups of existing users, membership commands, group locking by active group tickets, and group persistence.
- `individual-tickets`: Dated individual tickets with stored purchase price, runtime base price (`SYSTEM PRICE`), deletion rules, and ticket persistence.
- `group-tickets`: Group tickets composed of per-member individual tickets, discount brackets, all-or-nothing creation and deletion.
- `attraction-management`: Attractions with capacity, ticket-based entry and exit, status query, and persisted definitions.
- `uml-documentation`: The PlantUML class diagram of the implemented design at `uml/model.puml`.
- `temporary-persistence`: The `--fly` isolated execution mode with per-run storage directory and cleanup.

### Modified Capabilities
- `user-management`: User deletion is additionally blocked by tickets dated on or after the system date; a deleted user is removed from groups and attractions.
- `command-execution`: Mode selection accepts a leading `--fly` flag; `HELP` lists the F2 commands.

The source prompt mentions a `file-persistence` capability; the actual baseline is `user-persistence`, whose requirements do not change. Persistence of groups, tickets, and attractions is specified inside their own capabilities, and the storage-location switch inside `temporary-persistence`.

## Impact

- New domain classes (groups, ticket hierarchy, attractions), a park controller holding the system date and base price, new repositories, and command handlers in the shared `CommandProcessor`.
- New data files next to `users.txt`: `groups.txt`, `tickets.txt`, `attractions.txt` (existing `users.txt` format unchanged).
- New `uml/model.puml`; executable JAR renamed to `target/parque.jar` to match the documented invocations.
- Design decisions on the source's open questions (exit identifier, UML presentation, constituent ticket IDs, historical references, attraction lifecycle) are recorded in design.md.
