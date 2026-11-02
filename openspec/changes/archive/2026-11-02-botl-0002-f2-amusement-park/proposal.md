# Proposal

## Why

Delivery 1 manages only users. Delivery 2 turns the application into a stateful amusement-park domain: a single park controller with a runtime date and base price, groups of users, individual and group tickets with discounts, attractions with capacity-checked admission, complete text-file persistence with an isolated `--fly` test mode, and a PlantUML model faithful to the code. All Delivery 1 behavior must keep working.

## What Changes

- Add a single central park-management instance that owns users, groups, tickets and attractions, plus the current system date (initially the local start date) and base price (initially 40.00 EUR, per execution). New commands: `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`. (F2-C01)
- Add groups: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`. A group is locked while any associated group ticket is active (date ≥ system date). The lock is derived, never stored. (F2-C02, F2-C04)
- Add a ticket abstraction with one shared identifier space. `TICKET CREATE;<identifier>;<email>;<date>` creates an individual ticket at the current base price and displays the price. `TICKET DELETE;<identifier>` deletes only standalone tickets dated strictly after the system date. (F2-C03)
- Add group tickets: `GROUPTICKET CREATE;<identifier>;<group>;<date>` composes one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`, applies the bracket discount (0% / 10% / 20% / 50%), displays the total, and is all-or-nothing on identifier conflicts. `GROUPTICKET DELETE;<identifier>` deletes future group tickets together with their composition. (F2-C04)
- **Modify** `USER DELETE`: reject deletion while the user holds an active ticket (standalone or part of a group ticket) or belongs to a locked group. When deletion is allowed, remove the user from their groups. Historical tickets keep the deleted user's information. (F2-C03, F2-C04)
- Add attractions: `ATTRACTION CREATE;Identifier;Name;Capacity` and the read-only `ATTRACTION LIST;AttractionIdentifier`. Occupants are user references held by the attraction only. (F2-C05)
- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`, which uses the common ticket abstraction. The ticket must be valid for the current date. Group entry is atomic against capacity. Add `ATTRACTION EXIT;AttractionIdentifier;Email` and `ATTRACTION STATUS;AttractionIdentifier`. (F2-C06, F2-C07)
- Persist groups, tickets (with composition, holders and group reference) and attractions in text files next to the existing `users.txt`, but never occupants. Add the `--fly` first argument, which points the same repositories at `tmp/<yyyyMMdd_HHmmss>/` and deletes it when execution ends. (F2-C08)
- Add `uml/model.puml`, a class diagram that matches the implemented model. (F2-C09)
- **Modify** `HELP` to list every Delivery 1 and Delivery 2 command with the syntax from source §15. (F2-C10)

## Capabilities

### New Capabilities

- `park-control-and-system-settings`: Single park controller, system date and base price commands.
- `group-management`: Group lifecycle, membership and derived locking.
- `individual-ticket-management`: Ticket abstraction, shared identifier space, individual ticket creation and deletion.
- `group-ticket-purchasing`: Group ticket composition, derived identifiers, discounts, atomic purchase and deletion.
- `attraction-catalog-and-occupancy`: Attraction creation, occupant collection, read-only listing.
- `individual-attraction-entry`: Entering an attraction with a ticket valid today, with capacity checks.
- `atomic-group-entry-and-exit`: Indivisible group entry, individual exit, attraction status.
- `park-persistence-and-fly-mode`: Text-file persistence of the full park state (without occupants) and the `--fly` temporary mode.
- `plantuml-model`: `uml/model.puml` documenting the implemented design.
- `delivery-two-integration`: Delivery 2 technical restrictions and coexistence with Delivery 1.

### Modified Capabilities

- `user-deletion`: `USER DELETE` now also checks active tickets and locked groups, and removes the user from groups.
- `help-and-exit`: `HELP` lists all Delivery 1 and Delivery 2 command syntax.

## Impact

- Code: new domain classes (park controller, group, ticket hierarchy, attraction), new repositories, an extended command processor, and `App` argument handling for `--fly`. The Delivery 1 user-management logic moves into the park controller, with behavior unchanged.
- Data: new `groups.txt`, `tickets.txt` and `attractions.txt` beside `users.txt`. The `users.txt` format is unchanged.
- New file `uml/model.puml`. Still no runtime dependencies.
- Git: the source prescribes 10 milestone commits, but the BOTL harness owns history. See design.md.
