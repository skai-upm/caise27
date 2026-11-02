# Proposal

## Why

Delivery 1 only manages users. Delivery 2 (milestones F2-C01 to F2-C10) turns the application into a stateful amusement-park domain: a single central park controller with a runtime system date and base price, user groups, individual and group tickets sharing one abstraction and identifier space, attractions with capacity-checked entry and exit, persistence of all new elements, an isolated `--fly` temporary mode, and a PlantUML class model consistent with the code. The source requires this as one consolidated change.

## What Changes

- Add a single central park-management object (one instance per execution) holding users, groups, tickets, attractions, the current system date (initially the local start date) and the current base ticket price (initially 40.00 euros); add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` (F2-C01).
- Add groups with unique identifier, name and members: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`; membership changes and deletion are blocked while the group has an active group ticket, derived from tickets and the current date, never stored as a flag (F2-C02, F2-C04).
- Add a common ticket abstraction with a shared identifier space, `TICKET CREATE;<identifier>;<email>;<date>` (current base price, no past dates, price displayed) and `TICKET DELETE;<identifier>` (strictly future, not part of a group ticket) (F2-C03).
- Add `GROUPTICKET CREATE;<identifier>;<group>;<date>` composing one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`, discount brackets 0% / 10% / 20% / 50%, all-or-nothing identifier checks, total price displayed; add `GROUPTICKET DELETE;<identifier>` (strictly future, deletes the composition) (F2-C04).
- **Behaviour change to Delivery 1:** `USER DELETE` additionally rejects deletion when the user has an active ticket (individual or within a group ticket) or belongs to a locked group, and on success removes the user from their groups; historical tickets are retained (F2-C03, F2-C04).
- Add attractions with unique identifier, name, positive capacity and the collection of users inside: `ATTRACTION CREATE;Identifier;Name;Capacity` and read-only `ATTRACTION LIST;AttractionIdentifier` (F2-C05).
- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` for individual tickets (F2-C06) and, through the same mechanism, indivisible entry with group tickets; add `ATTRACTION EXIT;AttractionIdentifier;Email` and `ATTRACTION STATUS;AttractionIdentifier` (F2-C07).
- Persist groups, tickets (with their user/group/composition references) and attractions in text files alongside users, without serialization; occupants are not persisted. Add `--fly` as optional first argument: all repositories use a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory deleted at the end of execution (F2-C08).
- Add `uml/model.puml`, a PlantUML class diagram matching the implemented model (F2-C09).
- Extend `HELP` with all Delivery 1 and Delivery 2 commands and integrate everything end to end (F2-C10).

## Capabilities

### New Capabilities

- `park-control-and-system-settings`: Single central park controller, system date and base price (F2-C01).
- `group-management`: Groups and their membership rules (F2-C02).
- `individual-ticket-management`: Ticket abstraction, shared identifier space, individual tickets (F2-C03).
- `group-ticket-purchasing`: Group ticket composition, discounts and deletion (F2-C04).
- `attraction-catalog-and-occupancy`: Attractions and occupant listing (F2-C05).
- `individual-attraction-entry`: Entry with individual tickets (F2-C06).
- `atomic-group-entry-and-exit`: Indivisible group entry, exit and attraction status (F2-C07).
- `park-persistence-and-fly-mode`: Persistence of the extended park state and the `--fly` temporary mode (F2-C08).
- `plantuml-model`: PlantUML class model of the implementation (F2-C09).
- `delivery-two-integration`: Delivery 2 technical restrictions and end-to-end behaviour (F2-C10).

### Modified Capabilities

- `user-deletion`: `USER DELETE` must also respect active tickets and locked groups, and remove the deleted user from its groups.
- `help-and-exit`: `HELP` must list the Delivery 2 commands as well.
- `executable-application`: Launch modes extended with `--fly` and `--fly <file>`.

## Impact

- Code: new domain classes (park controller, group, ticket hierarchy, attraction), new repositories and a storage coordinator, command processor extended with new commands, `App` argument handling for `--fly`.
- Data files: `groups.txt`, `tickets.txt`, `attractions.txt` next to the existing `users.txt` (working directory); `tmp/<timestamp>/` while running with `--fly`.
- New file `uml/model.puml`.
- No new dependencies; Java 21 standard library only.
- Git: the source prescribes 10 separate milestone commits; see design.md.
