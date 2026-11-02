# Proposal

## Why

Delivery 1 manages only users. Delivery 2 (milestones F2-C01 through F2-C10) turns the application into a stateful amusement-park system: a single central park controller with a runtime date and base price, user groups, individual and group tickets with discounts, attractions with capacity-controlled entry and exit, persistence of the whole park state, an isolated `--fly` test mode, and a PlantUML model of the implemented design. All Delivery 1 behavior must keep working.

## What Changes

- Add a single central park management object (one instance per execution) that owns users, groups, tickets and attractions, the current system date (initially the local date) and the base ticket price (initially 40.00 euros); add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- Add groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, with lock state derived from active group tickets.
- Add a common ticket abstraction with a shared identifier space; individual tickets via `TICKET CREATE` / `TICKET DELETE`; group tickets via `GROUPTICKET CREATE` / `GROUPTICKET DELETE`, composed of one individual ticket per member (`<groupTicketIdentifier>:<dni>`), priced with the member-count discount brackets (0% / 10% / 20% / 50%).
- Add attractions: `ATTRACTION CREATE`, `ATTRACTION LIST`, `ATTRACTION ENTER` (individual or atomic group entry against capacity), `ATTRACTION EXIT`, `ATTRACTION STATUS`.
- **Behavior change** to `USER DELETE`: rejected while the user has an active ticket (standalone or within a group ticket) or belongs to a group locked by an active group ticket; on success the user is also removed from their groups. Historical tickets remain as fiscal records.
- Persist groups, tickets (including composition and group reference) and attractions in text files alongside users; attraction occupancy is never persisted.
- Add `--fly` mode: all repositories use a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted at the end of execution; supported with and without a command file.
- Extend `HELP` to list every Delivery 1 and Delivery 2 command.
- Add `uml/model.puml` describing the implemented classes, ticket hierarchy, composition, association directions and multiplicities.

## Capabilities

### New Capabilities
- `park-control-and-system-settings`: Single central park controller, current system date and base ticket price, `SYSTEM DATE` and `SYSTEM PRICE`.
- `group-management`: Groups of registered users, membership commands and lock rules derived from active group tickets.
- `individual-ticket-management`: Common ticket abstraction, shared identifier space, `TICKET CREATE` / `TICKET DELETE` and ticket activity rules.
- `group-ticket-purchasing`: `GROUPTICKET CREATE` / `GROUPTICKET DELETE`, composition of individual tickets, derived identifiers and discount brackets.
- `attraction-catalog-and-occupancy`: Attractions, `ATTRACTION CREATE` and the read-only `ATTRACTION LIST`.
- `individual-attraction-entry`: `ATTRACTION ENTER` with a ticket valid for the current date and available capacity.
- `atomic-group-entry-and-exit`: All-or-nothing group entry, `ATTRACTION EXIT` and `ATTRACTION STATUS`.
- `park-persistence-and-fly-mode`: Text-file persistence of the full park state and the temporary `--fly` execution mode.
- `plantuml-model`: `uml/model.puml` documenting the implemented design.
- `delivery-two-integration`: Delivery 2 technical restrictions and integrated behavior of both deliveries.

### Modified Capabilities
- `help-and-exit`: `HELP` must list the Delivery 1 and Delivery 2 command syntax.
- `user-deletion`: `USER DELETE` must respect active tickets and locked groups and remove the deleted user from modifiable groups.

## Impact

- Code: new domain packages (park, group, ticket, attraction), new storage classes in `persistence`, `CommandProcessor` delegating to the central park object, `App` argument handling for `--fly`; `UserManager` updates users in place so other objects keep valid references.
- Data files (normal mode, working directory): `users.txt` (unchanged format), new `groups.txt`, `tickets.txt`, `attractions.txt`.
- New file `uml/model.puml`.
- No new runtime dependencies (Java 21 standard library only).
- Git: the source prescribes 10 milestone commits with exact messages; the batch harness controls Git history (see design.md).
