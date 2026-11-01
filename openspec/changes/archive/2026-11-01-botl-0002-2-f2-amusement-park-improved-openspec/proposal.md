# Proposal

## Why

F1 delivered user management, file persistence and terminal/file command execution. Delivery 2 (F2) must turn the application into a park-operations tool: date-dependent rules, groups of users, individual and group tickets with discounts, attractions with capacity-controlled access, a UML model of the real code, and an isolated `--fly` run mode for testing. These features interact (tickets block user deletion and lock groups; attractions accept both ticket types), so they are delivered as one coherent change that keeps every F1 behavior intact.

## What Changes

- **System date**: one runtime date, initialized from the OS date at startup and changeable with `SYSTEM DATE;yyyy-MM-dd`; every date-dependent rule uses it.
- **Groups**: `GROUP CREATE/ADD/REMOVE/DELETE`; groups reference existing users, reject unknown/duplicate members, and persist.
- **Individual tickets**: `TICKET CREATE` (replies `OK TICKET CREATED -> <Precio>`), `TICKET DELETE` (only strictly future tickets), runtime base price (40.00 per run, `SYSTEM PRICE;<price>`), purchase price stored with the ticket, historical tickets retained.
- **Group tickets**: `GROUPTICKET CREATE` (replies `OK GROUP TICKET CREATED -> <Precio>`) generating one constituent ticket per current member with size-bracket discounts; `GROUPTICKET DELETE` (strictly future, all-or-nothing); groups with current/future group tickets are locked.
- **Attractions**: `ATTRACTION CREATE/ENTER/EXIT/STATUS`; capacity enforced, group entry atomic, entry requires a ticket for the current date, exit does not re-check the date, occupancy is runtime-only.
- **UML**: `uml/model.puml` derived from the implemented code.
- **`--fly` mode**: all repositories use a per-run `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted on termination.
- **Modified F1 behavior**: `USER DELETE` is rejected while the user has a current/future ticket (also removing deleted users from groups); `HELP` lists F2 commands; command-line arguments accept `--fly` alongside the optional command file.
- Out of scope: F3 SpeedPass, Special Pass, invoice Builder, BCrypt, attraction categories, Singleton adoption, web/API interfaces, databases.

## Capabilities

### New Capabilities
- `system-date`: Single configurable runtime date used by all date-dependent rules.
- `group-management`: Groups of existing users, membership rules and group persistence.
- `individual-tickets`: Dated individual tickets, runtime base price, ticket persistence, history and refund rules.
- `group-tickets`: Group tickets composed of per-member tickets, discount brackets, group locking and all-or-nothing deletion.
- `attraction-management`: Attractions, capacity-controlled individual/group entry, exit and status; definitions persist, occupancy does not.
- `uml-documentation`: PlantUML class model in `uml/model.puml` reflecting the implemented code.
- `temporary-persistence`: `--fly` execution with per-run temporary storage directory and cleanup.

### Modified Capabilities
- `user-management`: `USER DELETE` additionally rejected while the user holds a current/future ticket or belongs to a locked group; a deleted user is removed from groups and attractions.
- `command-execution`: Interactive/file mode selection accepts the `--fly` flag; `HELP` lists every F2 command.

(The prompt's suggested `file-persistence` capability does not exist in this project; persistence of groups, tickets and attractions is specified inside their own new capabilities, and `user-persistence` is unchanged.)

## Impact

- New domain classes (groups, ticket hierarchy, attractions), managers and file storages under `es.upm.amusementpark`; a central controller holding the system date and base price.
- New data files next to `users.txt`: `groups.txt`, `tickets.txt`, `attractions.txt` (or inside `tmp/<timestamp>/` with `--fly`).
- `CommandProcessor` now delegates to the controller; `App` parses `--fly`.
- Executable JAR renamed to `target/parque.jar` to match the documented examples.
- New `uml/model.puml`; `tmp/` added to `.gitignore`.
- Open interpretation points (attraction exit identifier, UML display rules, constituent ticket IDs, historical references, attraction lifecycle) are resolved and recorded in design.md.
