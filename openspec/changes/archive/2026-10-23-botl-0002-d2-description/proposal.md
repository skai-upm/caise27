# Proposal

## Why

Delivery 2 of the Amusement Park assignment turns the Delivery 1 user-management foundation into a park: users form groups, buy individual or discounted group tickets for a date, and use them to enter attractions. The park state must be centrally managed, survive restarts, and be testable in isolation (`--fly`) and against a controllable system date and price.

## What Changes

- Add a single central park manager holding the current system date (initially today) and base ticket price (initially 40.00), with `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- Add user groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, locked (derived, not stored) while an active group ticket exists.
- Add tickets sharing one identifier space: individual tickets (`TICKET CREATE`/`TICKET DELETE`) and group tickets (`GROUPTICKET CREATE`/`GROUPTICKET DELETE`) composed of per-member individual tickets `<id>:<dni>` with member-count discounts (0/10/20/50%); only tickets dated strictly after today can be deleted.
- Add attractions with capacity and occupants: `ATTRACTION CREATE`, `ENTER` (by ticket valid today; group entry all-or-nothing), `EXIT`, `LIST`, `STATUS`.
- **Modify** `USER DELETE`: reject users with active tickets (individual or group component) or belonging to a locked group; otherwise remove them from their groups. Historical tickets keep the deleted user's data.
- Extend persistence to groups, tickets (including historical records of deleted users/groups) and attractions (occupants are not persisted).
- Add `--fly` temporary persistence mode (timestamped `tmp/` directory deleted at the end of execution), usable interactively and with a command file.
- **Modify** `HELP` to list every Delivery 1 and Delivery 2 command.
- Add a PlantUML class model at `uml/model.puml`.

## Capabilities

### New Capabilities
- `park-settings`: Current system date and base ticket price, and the commands that change them.
- `user-groups`: Group creation, membership changes, deletion and derived locking by active group tickets.
- `park-tickets`: Individual and group tickets, shared identifier space, group discounts, creation and deletion rules, active/historical classification.
- `attractions`: Attraction creation, entry by ticket with capacity control, exit, occupant listing and status.
- `park-persistence`: Storing and reconstructing groups, tickets (including historical ones) and attractions between runs, and the `--fly` temporary data mode.

### Modified Capabilities
- `command-interface`: `HELP` must list all Delivery 1 and 2 commands; execution accepts `--fly` as an optional first argument.
- `user-management`: `USER DELETE` must respect active tickets and locked groups and remove the user from groups.

## Impact

- Code: new model classes (groups, ticket hierarchy, attractions), central park manager, services, repositories; `CommandProcessor` and `App` extended; `UserService` deletion moved behind park-level checks.
- Data files: `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt` in the data directory (working directory by default).
- New file `uml/model.puml`; README updated.
