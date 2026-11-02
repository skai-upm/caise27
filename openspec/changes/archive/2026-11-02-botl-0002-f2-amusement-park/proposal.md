# Proposal

## Why

F1 delivers users, persistence, and text-command execution. Delivery 2 (F2) must turn this into a working park: date-dependent operations, user groups, individual and group tickets with discounts, attractions with capacity-controlled access, a PlantUML model of the real code, and an isolated `--fly` execution mode for testing without touching permanent data. These features interact (tickets lock users and groups, attractions consume tickets), so they are delivered as one coherent change that preserves all F1 behavior.

## What Changes

- **System date**: one runtime date held by the application controller, initialized to the OS local date, changed with `SYSTEM DATE;yyyy-MM-dd`; every date-dependent rule uses it.
- **Base price**: runtime base ticket price initialized to `40.00`, changed with `SYSTEM PRICE;<price>`; purchased tickets keep the price they were sold at.
- **Groups**: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`; groups reference existing users, reject unknown/duplicate members, and persist.
- **Individual tickets**: `TICKET CREATE` (reports `OK TICKET CREATED -> <Precio>`), `TICKET DELETE` (future tickets only); tickets persist and remain as history.
- **Group tickets**: `GROUPTICKET CREATE` (one constituent ticket per current member, size-bracket discount, reports `OK GROUP TICKET CREATED -> <Precio>`), `GROUPTICKET DELETE` (future only, all-or-nothing); a group with a current/future group ticket is locked.
- **Attractions**: `ATTRACTION CREATE`, `ATTRACTION ENTER` (ticket valid for the system date, capacity respected, group entry atomic), `ATTRACTION EXIT`, `ATTRACTION STATUS`; definitions persist, occupancy is runtime-only.
- **UML**: `uml/model.puml` derived from the implemented code.
- **`--fly`**: per-run timestamped `tmp/<yyyyMMdd_HHmmss>/` directory used by every repository, deleted on termination (including `EXIT`).
- **Modified F1 behavior**: `USER DELETE` is rejected while the user has a ticket dated on or after the system date; `HELP` lists the F2 commands; argument handling accepts `--fly`.

Out of scope: F3 features (SpeedPass, Special Pass, invoice Builder, BCrypt, attraction categories), Singleton as a requirement, web/API interfaces, databases.

## Capabilities

### New Capabilities
- `system-date`: Single configurable runtime date and the base ticket price used by date- and price-dependent rules.
- `group-management`: Groups of existing users, membership rules, locking by active group tickets, and group persistence.
- `individual-tickets`: Dated individual tickets with recorded price, deletion rules, history, and persistence.
- `group-tickets`: Group ticket purchase composed of constituent tickets, discount brackets, atomic deletion, and persistence.
- `attraction-management`: Attraction definitions, capacity-controlled individual/group entry, exit, status, and definition persistence.
- `uml-documentation`: PlantUML class diagram of the implemented model in `uml/model.puml`.
- `temporary-persistence`: `--fly` execution with an isolated, self-deleting persistence directory.

### Modified Capabilities
- `user-management`: "Delete user" is additionally blocked by tickets dated on or after the system date, and deleted users leave their groups.
- `command-execution`: interactive/file mode selection accepts a leading `--fly` flag, and `HELP` lists all F2 commands.

## Impact

- New domain, manager, and storage classes under `com.amusementpark`; `CommandProcessor` dispatches through a new controller that owns the system date and base price.
- New persistence files next to `users.txt`: `groups.txt`, `tickets.txt`, `group_tickets.txt`, `attractions.txt` (existing `users.txt` format unchanged).
- Build output renamed to `target/parque.jar` to match the documented `java -jar target/parque.jar --fly` examples.
- New `uml/model.puml`. No new runtime dependencies.
- Open questions from the request (exit identifier, UML display conflict, constituent ticket IDs, historical references, attraction lifecycle) are resolved and recorded in `design.md`.
