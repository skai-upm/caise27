# Proposal

## Why

Delivery 1 gave the Amusement Park application user management, a command interface and file persistence. Delivery 2 introduces the first park-specific features: user groups, individual and group tickets (with group discounts), attractions with capacity and occupancy, and a configurable system date and ticket price. These features relate users, groups, tickets and attractions to each other, so the existing user deletion rules, persistence and execution modes must be extended consistently, and a single central park manager must coordinate the shared state.

## What Changes

- Add a single central park manager (exactly one instance per execution) that owns users, groups, tickets, attractions, the current system date (initially today's local date) and the current base ticket price (initially 40.00 EUR).
- Add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` commands; neither value is persisted between runs.
- Add user groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`. A group is locked (no add/remove/delete) while it has an active group ticket (date on or after the system date); the locked state is derived, never stored.
- Add tickets as a common abstraction with two concrete types sharing one identifier space and price behaviour:
  - individual tickets: `TICKET CREATE`, `TICKET DELETE` (only for dates strictly after the system date; not for tickets inside a group ticket);
  - group tickets: `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, composed of one individual ticket per member with derived identifiers `<groupTicketId>:<dni>`, discounted by group size (0% / 10% / 20% / 50%), created atomically.
- Add attractions: `ATTRACTION CREATE`, `ATTRACTION ENTER` (individual or group ticket valid for the system date; group entry is all-or-nothing within capacity), `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`.
- **BREAKING (behaviour)** `USER DELETE` now fails while the user has an active ticket (individual or inside a group ticket) or belongs to a locked group; on success the user is also removed from their groups, while historical tickets keep the user's information.
- Extend persistence to groups, tickets (including group ticket composition and the possibly-null group reference) and attractions, using text files and the same separation of responsibilities. Attraction occupants are not persisted.
- Add the `--fly` temporary persistence mode: all repositories use a fresh timestamped directory under `tmp/`, deleted when execution ends; usable interactively or with a command file.
- Extend `HELP` with all new commands.
- Add a PlantUML class diagram at `uml/model.puml` that matches the implemented model.

Out of scope: the course's mandatory GitLab milestone commit messages (`[F2-C01]` … `[F2-C10]`); repository history is produced by the batch orchestrator, one commit per proposal.

## Capabilities

### New Capabilities
- `park-settings`: The current system date and base ticket price, their initial values and the `SYSTEM DATE` / `SYSTEM PRICE` commands.
- `user-groups`: Creating groups, adding/removing members, deleting groups, and the derived lock caused by active group tickets.
- `tickets`: Individual and group tickets: shared identifier space, creation and deletion rules, active vs historical tickets, group composition and discounts.
- `attractions`: Creating attractions, entering with a ticket, leaving, and listing/status of occupants.
- `park-persistence`: Text-file storage and reconstruction of groups, tickets and attractions between runs.

### Modified Capabilities
- `application-runtime`: Interactive and file modes accept a leading `--fly` argument; new temporary persistence mode requirement.
- `command-interface`: The set of supported commands and the `HELP` output include all Delivery 2 commands.
- `user-management`: `USER DELETE` is restricted by active tickets and locked groups and removes the user from their groups.

## Impact

- Code: new central `Park` manager and domain classes (groups, ticket hierarchy, attractions), new repositories, a storage configuration that selects the data directory, extended `CommandProcessor` and `App` argument handling. `UserService` is reused for user CRUD.
- Data: new files `groups.txt`, `tickets.txt`, `attractions.txt` next to `users.txt` (working directory by default; a temporary directory in `--fly` mode). Existing `users.txt` files remain compatible.
- The `park.users.file` system property used by tests is replaced by a data-directory property (`park.data.dir`).
- New file `uml/model.puml`. No new runtime dependencies.
