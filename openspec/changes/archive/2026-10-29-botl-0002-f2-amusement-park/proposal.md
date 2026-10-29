# Proposal

## Why

Delivery 2 of the Amusement Park assignment extends the Delivery 1 user-management application with central park state (system date and ticket price), user groups, individual and group tickets with discounts, attractions with capacity-controlled access, full-park persistence including a temporary `--fly` mode, and a PlantUML class model. This change implements milestones F2-C01 through F2-C10 together, on top of the Delivery 1 code.

## What Changes

- **F2-C01**: Add a single, globally unique park controller that holds the users, groups, tickets and attractions, the current system date (initially the local date at start-up), and the current base price (initially 40.00 €). New commands: `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- **F2-C02**: Add `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email` and `GROUP REMOVE;Identifier;Email`. A user cannot appear twice in a group. A group is locked while it has an active group ticket; this state is derived, not stored.
- **F2-C03**: Add a common ticket abstraction (shared identifier space, date, price behaviour), `TICKET CREATE;<id>;<email>;<date>` (charges the current price and prints it) and `TICKET DELETE;<id>` (only for dates strictly after today, never for group-composition tickets). **BREAKING (behavioural)**: `USER DELETE` is now rejected while the user has an active ticket.
- **F2-C04**: Add `GROUPTICKET CREATE;<id>;<group>;<date>`. Non-empty groups only. The ticket is composed of one individual ticket `<id>:<dni>` per member, with a member-count discount (0/10/20/50 %); the total is printed and the purchase is all-or-nothing. Also add `GROUPTICKET DELETE;<id>` (future dates only; it also deletes the composition) and `GROUP DELETE;<id>` (rejected while an active group ticket exists; historical group tickets keep their data with a null group reference). `USER DELETE` additionally checks group-ticket compositions and locked groups, and removes the user from modifiable groups.
- **F2-C05**: Add `ATTRACTION CREATE;Identifier;Name;Capacity` and the read-only `ATTRACTION LIST;Identifier` (emails of the current occupants).
- **F2-C06/F2-C07**: Add `ATTRACTION ENTER;Attraction;Ticket` for individual and group tickets through the common ticket abstraction. The ticket date must equal today, entry respects capacity, and group entry is all-or-nothing. Add `ATTRACTION EXIT;Attraction;Email` and `ATTRACTION STATUS;Attraction`.
- **F2-C08**: Persist groups, tickets (including historical tickets of deleted users or groups) and attractions in text files, with occupants never persisted. Add the `--fly` mode, in which the same repositories point to a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted at the end of execution.
- **F2-C09**: Add `uml/model.puml` documenting the Delivery 2 model.
- **F2-C10**: Extend `HELP` to the full Delivery 2 syntax list, and integrate and verify the whole feature set.

## Capabilities

### New Capabilities
- `park-state`: Single central park controller, current system date and current base ticket price.
- `user-groups`: Group creation, membership changes, deletion and derived lock state.
- `tickets`: Common ticket abstraction, individual tickets, group tickets with composition and discounts, deletion rules, active/historical status.
- `attractions`: Attraction creation, occupancy, entry with tickets, exit, list and status.
- `park-persistence`: Text-file persistence of groups, tickets and attractions, and the temporary `--fly` persistence mode.
- `design-documentation`: PlantUML class model kept in the repository.

### Modified Capabilities
- `user-management`: User deletion now also depends on active tickets and locked groups, and removes the user from groups.
- `command-interface`: HELP lists the Delivery 2 commands; the accepted command-line argument forms gain `--fly`.

## Impact

- New domain classes (controller, group, ticket hierarchy, attraction) and repositories. `CommandProcessor` and `App` are extended, and `UserService` gains authentication and removal hooks used by the controller.
- New data files `groups.txt`, `tickets.txt` and `attractions.txt` sit next to `users.txt`. The existing `users.txt` format is unchanged.
- New `uml/model.puml`.
- Git: the source requests ten separate milestone commits. BOTL produces one commit per proposal file, so the messages are recorded in that commit's body (flagged, as for Delivery 1).
