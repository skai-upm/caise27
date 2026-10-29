# Proposal

## Why

Delivery 1 manages only users. Amusement Park Delivery 2 (milestones F2-C01 to F2-C10) turns the application into a park manager: a single central controller with a configurable system date and ticket price, user groups, individual and group tickets with discounts, attractions with capacity-checked access, persistence of the whole park state, a temporary `--fly` execution mode and a PlantUML class model.

## What Changes

- **F2-C01** Add a single-instance park controller holding users, groups, tickets, attractions, the current system date (initially the local date at start-up) and the current base price (initially 40.00 €). Add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- **F2-C02** Add groups: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, with no duplicate members and modification blocked by active group tickets (derived, not stored).
- **F2-C03** Add a common ticket abstraction and `TICKET CREATE;<identifier>;<email>;<date>` / `TICKET DELETE;<identifier>` using the current price, a shared identifier space, no past dates and deletion only for dates strictly after today. **BREAKING (behavioural):** `USER DELETE` is rejected while the user has an active ticket.
- **F2-C04** Add `GROUPTICKET CREATE;<identifier>;<group>;<date>` composed of `<groupTicketIdentifier>:<dni>` individual tickets with member-count discounts (0/10/20/50 %), `GROUPTICKET DELETE;<identifier>` and `GROUP DELETE;Identifier`. `USER DELETE` also checks group-ticket compositions and locked groups, and removes the user from modifiable groups.
- **F2-C05** Add attractions: `ATTRACTION CREATE;Identifier;Name;Capacity` and `ATTRACTION LIST;AttractionIdentifier`.
- **F2-C06/F2-C07** Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` (individual and group tickets through the common abstraction, atomic capacity check), `ATTRACTION EXIT;AttractionIdentifier;Email` and `ATTRACTION STATUS;AttractionIdentifier`.
- **F2-C08** Persist groups, tickets (including composition, user data needed for historical records and the group reference) and attractions in text files; occupants are not persisted. Add `--fly` mode using the same repositories pointed at `tmp/<yyyyMMdd_HHmmss>/`, deleted at the end of execution.
- **F2-C09** Add `uml/model.puml` with the class model.
- **F2-C10** Extend HELP with all Delivery 2 commands and verify everything works together.

## Capabilities

### New Capabilities
- `park-state`: Central park management (single instance), current system date and current base price.
- `user-groups`: Group creation, membership changes, deletion and derived locking.
- `ticketing`: Individual and group tickets, shared identifier space, pricing and discounts, deletion and historical records.
- `attractions`: Attraction creation, entry, exit, listing and status.
- `park-persistence`: Text-file persistence of groups, tickets and attractions, and the `--fly` temporary execution mode.
- `design-model`: PlantUML class model of the design.

### Modified Capabilities
- `command-processing`: Recognised command set and HELP output extended with Delivery 2 commands.
- `user-management`: USER DELETE now considers active tickets and group locks and removes the user from groups.

## Impact

- Code: new domain classes (park controller, group, ticket hierarchy, attraction), new repositories, `App` argument handling, `CommandProcessor` dispatch, `UserService` deletion split.
- New runtime files `groups.txt`, `tickets.txt`, `attractions.txt` beside `users.txt`; temporary `tmp/` directory for `--fly` (both git-ignored).
- New file `uml/model.puml`.

## Ambiguities flagged (not silently resolved)

1. **Milestone commits**: ten exact-message commits are requested; the BOTL batch produces one commit for this proposal and lists the milestone messages in its body.
2. **SYSTEM PRICE format**: "valid, positive monetary amount" is interpreted as digits with an optional `.` and at most two decimals, greater than zero.
3. **Identifier syntax** is not specified; empty identifiers are rejected, nothing else is restricted.
4. **Entering with a composition ticket** (`TG01:12345678Z`): it is an individual ticket valid for its date, so ATTRACTION ENTER accepts it and admits only that user.
5. **Re-entering the same attraction**: a user already inside an attraction cannot be added again (the occupant collection would double count capacity); entry is rejected for the whole ticket. Being inside several attractions is not restricted (not specified).
6. **ATTRACTION EXIT** for a user not inside the attraction is rejected with an error.
7. **Attraction capacity** must be a positive integer; attraction identifiers must be unique (stated) and attractions cannot be deleted (no command defined).
8. **`--fly` directory collision**: if `tmp/<timestamp>/` already exists, a numeric suffix is appended. The `tmp/` parent is removed too when it becomes empty.
9. **Discount rounding**: unit prices are rounded to cents (half-up); the group total is the sum of unit prices.
