# Proposal

## Why

F1 delivers users, file persistence and text-command execution. Delivery 2 (F2) turns this into a park-operations application: date-dependent rules, user groups, individual and group tickets with discounts, attractions with capacity-controlled access, a UML model derived from the code, and an isolated `--fly` execution mode. These features interact (tickets lock users and groups, attractions consume tickets), so they are specified and delivered as one coherent change while preserving all F1 behavior.

## What Changes

- **System date and price**: one runtime system date (initialised to today, changed with `SYSTEM DATE;yyyy-MM-dd`) used by every date rule; runtime base ticket price (initially `40.00`, changed with `SYSTEM PRICE;60.00`).
- **Groups**: `GROUP CREATE/ADD/REMOVE/DELETE`; groups reference existing users; persisted and restored.
- **Individual tickets**: `TICKET CREATE` (returns `OK TICKET CREATED -> <Precio>`), `TICKET DELETE` (only strictly future tickets); price fixed at purchase; persisted; past tickets kept as history.
- **Group tickets**: `GROUPTICKET CREATE` (one constituent ticket per current member, size-bracket discounts, returns `OK GROUP TICKET CREATED -> <Precio>`), `GROUPTICKET DELETE` (strictly future, all-or-nothing); groups with current/future group tickets are locked.
- **Attractions**: `ATTRACTION CREATE/ENTER/EXIT/STATUS`; capacity enforced; group entry atomic; tickets valid only on the system date; occupancy runtime-only; definitions persisted.
- **User deletion (modified)**: rejected while the user holds a ticket dated on or after the system date; a deleted user is removed from the groups it belonged to.
- **Command execution (modified)**: new commands registered and listed by `HELP`; optional leading `--fly` argument.
- **Temporary persistence**: `--fly` routes every repository to `tmp/<yyyyMMdd_HHmmss>/` and deletes it on termination.
- **UML**: `uml/model.puml` generated from the implemented code.

Out of scope: F3 features (SpeedPass, Special Pass, invoice builder, BCrypt, attraction categories), Singleton adoption, databases, web/API interfaces.

## Capabilities

### New Capabilities
- `system-date`: Single configurable runtime date used by all date-dependent rules.
- `group-management`: Groups of existing users, membership rules, group persistence.
- `individual-tickets`: Dated individual tickets, runtime base price, refund rule, ticket persistence and history.
- `group-tickets`: Group ticket composition, discount pricing, refund rule and group locking.
- `attraction-management`: Attraction definitions, capacity-controlled entry, exit and status.
- `temporary-persistence`: `--fly` isolated storage directory and clean-up.
- `uml-documentation`: PlantUML class model reflecting the implemented code.

### Modified Capabilities
- `user-management`: user deletion is blocked by current/future tickets and removes the user from groups.
- `command-execution`: `HELP` lists the F2 commands; interactive and file modes accept a leading `--fly` argument.

## Impact

- New domain classes (groups, ticket hierarchy, attractions), a park controller holding the system date and base price, new repositories sharing one text-file data-access helper, new data files (`groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`) next to `users.txt`.
- `App` argument handling and `CommandProcessor` dispatch extended; F1 command syntax and `users.txt` format unchanged.
- New `uml/model.puml`; `.gitignore` extended for the new data files and `tmp/`.
- Open interpretation decisions (attraction exit identifier, UML presentation rules, constituent ticket identifiers, historical references, attraction lifecycle) are recorded in design.md.
