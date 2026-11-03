# Proposal

## Why

Delivery 1 only manages users. Delivery 2 of the Amusement Park assignment (milestones F2-C01…F2-C10, source archived verbatim in `.botl/proposals/…/2.F2_Amusement_Park_Improved_OpenSpec.md`, Appendix A) turns it into a stateful park: a single central park manager with a settable system date and base price, user groups, individual and discounted group tickets with date-derived locking, attractions with capacity-checked (and, for groups, all-or-nothing) entry, persistence of the new elements, an isolated `--fly` temporary mode for repeatable test runs, and a PlantUML model faithful to the code. All Delivery 1 behavior must keep working.

## What Changes

- Add a single central park manager (one instance per execution) holding users, groups, tickets and attractions plus the current system date (initially the local date) and base price (initially 40.00 EUR); add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` (price affects only later purchases and is not kept between executions).
- Add groups: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`, with unique identifiers, no duplicate members, and a lock derived (never stored) from associated group tickets dated on or after the system date.
- Add a ticket hierarchy with a shared identifier space and reused price behavior: `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`. Group tickets are composed of one individual ticket per member with identifier `<groupTicketIdentifier>:<dni>` and discounts of 0 / 10 / 20 / 50 % by member count; creation is atomic and displays the price; only strictly future tickets can be deleted.
- **BREAKING (behavioral, required by source §9):** `USER DELETE` is now rejected while the user has an active ticket (individual or inside a group ticket) or belongs to a locked group; when allowed it also removes the user from their groups. Historical tickets of deleted users and groups are retained.
- Add attractions: `ATTRACTION CREATE;Identifier;Name;Capacity`, `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` (ticket valid for the current date, capacity check, group entry indivisible), `ATTRACTION EXIT;AttractionIdentifier;Email`, `ATTRACTION LIST;AttractionIdentifier`, `ATTRACTION STATUS;AttractionIdentifier`. Occupants are kept in memory only.
- Persist groups (with members), tickets (with holders, compositions and group references) and attractions in text files alongside users, without serialization, databases or libraries; occupants are not persisted.
- Add `--fly` (first argument, optionally followed by a command file): all repositories use a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when execution ends; without `--fly` argument handling is unchanged.
- Add `uml/model.puml` documenting the implemented model.
- Extend `HELP` with every Phase 1 and Phase 2 command.

## Capabilities

### New Capabilities
- `park-control-and-system-settings`: Single central park manager, current system date and base ticket price with `SYSTEM DATE` / `SYSTEM PRICE`.
- `group-management`: Group creation, membership changes and deletion with derived ticket-based locking.
- `individual-ticket-management`: Common ticket abstraction and identifier space; individual ticket creation, pricing and deletion; ticket-based restrictions on user deletion.
- `group-ticket-purchasing`: Atomic group ticket purchase composed of member tickets, derived identifiers, discount brackets, group ticket deletion.
- `attraction-catalog-and-occupancy`: Attraction creation with unique identifier and positive capacity; read-only occupant listing.
- `individual-attraction-entry`: Ticket-based attraction entry validated against the current date and capacity.
- `atomic-group-entry-and-exit`: Indivisible group-ticket entry, per-user exit and attraction status.
- `park-persistence-and-fly-mode`: Text-file persistence of groups, tickets and attractions with relationships; non-persistent occupancy; `--fly` temporary mode.
- `plantuml-model`: `uml/model.puml` class model consistent with the implementation.
- `delivery-two-integration`: Delivery 2 technical restrictions (object relationships and directions, single park instance, no serialization) and coexistence of Phase 1 and Phase 2 commands.

### Modified Capabilities
- `help-and-exit`: `HELP` must list the Phase 2 commands in addition to the Phase 1 ones, using the Delivery 2 syntax listing.
- `user-deletion`: `USER DELETE` must additionally respect active tickets and locked groups and remove the deleted user from their groups.

## Impact

- Code: new domain classes (park manager, group, ticket hierarchy, attraction), new services and repositories, extended command processor and `App` argument handling; `UserService` moves its user collection into the park manager.
- Data: new text files next to `users.txt` (`groups.txt`, `tickets.txt`, `attractions.txt`) in the data directory; `--fly` creates and removes `tmp/<timestamp>/`.
- New file: `uml/model.puml`.
- Dependencies: none added (Java standard library only; JUnit remains test-scoped).
- Git: the source prescribes ten milestone commit messages; handling under the batch harness is recorded in design.md.
