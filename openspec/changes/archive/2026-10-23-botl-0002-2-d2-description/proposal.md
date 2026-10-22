# Proposal

## Why

Delivery 1 gives the Amusement Park application user management only. Delivery 2 (source: `.botl/proposals/opus_4_description_nl/0002-2.D2.-description.md/2.D2.-description.md`) adds the first park-specific features: user groups, individual and group tickets, and attractions that users enter and leave with tickets. All Delivery 1 behavior is kept. The model must show clear object relationships: a ticket hierarchy, group tickets composed of individual tickets, and one-directional references. State changes depend on a configurable current date, so the delivery also needs controllable system settings, complete persistence that preserves historical (fiscal) ticket records, and a disposable `--fly` execution mode for repeatable tests.

## What Changes

- A single central park controller that owns users, groups, tickets, attractions, the current system date (initially today's local date), and the current base ticket price (initially 40.00 EUR). New commands: `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<price>`.
- User groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`. A group is locked against changes while it has an active group ticket (dated today or later). The lock is derived from tickets, not stored.
- Tickets with a common abstraction and one shared identifier space:
  - `TICKET CREATE` and `TICKET DELETE` for individual tickets.
  - `GROUPTICKET CREATE` and `GROUPTICKET DELETE` for group tickets. A group ticket is composed of one individual ticket per member, with ids `<groupTicketId>:<dni>`.
  - Bracket discounts: 0%, 10%, 20% or 50% by member count.
  - Only tickets dated strictly in the future can be deleted.
- Attractions: `ATTRACTION CREATE`, `ENTER` (individual or group ticket, all-or-nothing within capacity), `EXIT`, `LIST`, and `STATUS`. Occupants are held only by the attraction and are not persisted.
- **BREAKING (behavioral):** `USER DELETE` is now rejected while the user has an active ticket or belongs to a group locked by an active group ticket. A permitted deletion also removes the user from their groups and from any attraction they are in. Historical tickets of deleted users are preserved.
- Persistence extended to groups, tickets (including historical records whose user or group no longer exists), and attractions, in text files that are reloaded on startup. Attractions always start empty.
- `--fly` execution mode. It optionally precedes a command file, keeps all data in a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory using the same repositories, and deletes that directory when the run ends.
- `HELP` lists every Delivery 1 and Delivery 2 command.
- A PlantUML class model in `uml/model.puml` that matches the implementation.

## Capabilities

### New Capabilities
- `park-settings`: Current system date and current base ticket price: initial values, the `SYSTEM DATE` and `SYSTEM PRICE` commands, and their effect on date- and price-dependent operations.
- `user-groups`: Creating, populating, and deleting groups of users, and the active-group-ticket lock that restricts group changes.
- `tickets`: The shared ticket model: identifier space, active and historical status, individual ticket creation and deletion, and the effect of tickets on what users may do.
- `group-tickets`: Group ticket purchase as a composition of per-member individual tickets, derived identifiers, discount brackets, and deletion rules.
- `attractions`: Attractions, their capacity and occupants, and entering, leaving, listing, and status queries.
- `park-persistence`: Storing and reloading groups, tickets (including historical records), and attractions, plus the temporary `--fly` storage mode and its cleanup.
- `design-model`: The PlantUML class diagram documenting the implemented model.

### Modified Capabilities
- `application-build`: Execution mode selection gains the optional leading `--fly` argument.
- `command-interface`: The recognized command keywords and the HELP output now include the Delivery 2 commands.
- `user-management`: `USER DELETE` now honors active tickets and locked groups and cleans up group membership and attraction presence.
- `user-persistence`: The user file lives in the active data directory (the working directory, or the temporary directory in `--fly` mode).

## Impact

- Code: a new central `Park` controller and new model classes (`Group`, `Ticket` hierarchy, `Attraction`), new file repositories with a shared data store, a reworked command processor (table-driven dispatch), and `App` argument handling for `--fly`. `UserService` gains an authentication accessor, and `CommandProcessor` now depends on the park controller.
- Data files: `users.txt` (unchanged format), plus `groups.txt`, `tickets.txt`, and `attractions.txt` in the data directory. Existing Delivery 1 `users.txt` files keep loading unchanged.
- New file: `uml/model.puml`.
- Dependencies: still Java standard library only at runtime, with JUnit 5 for tests.
- Out of scope: the assignment's mandatory `[F2-Cxx]` Git milestone commits. BOTL produces one commit per proposal; the milestones are reflected in the task groups instead.
