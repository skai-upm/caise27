# Proposal

## Why

Delivery 2 (F2) turns the user-management application into a park system: time-dependent rules need a controllable date, users must be organised into groups, buy individual and group tickets, and use them to enter attractions. The design must be documented in PlantUML, and a `--fly` mode is needed to run and test everything without touching the permanent data.

## What Changes

- **System date and price**: `SYSTEM DATE;yyyy-MM-dd` sets the single application date (initially the OS date, not persisted) held by the main controller; every date rule uses it. `SYSTEM PRICE;60.00` changes the individual ticket price (initially 40.00 each execution, not persisted).
- **Groups**: `GROUP CREATE;G01;Nombre`, `GROUP ADD;G01;correo`, `GROUP REMOVE;G01;correo`, `GROUP DELETE;G01`. Groups reference existing users (no copies), reject unknown or duplicate members, and are persisted.
- **Individual tickets**: `TICKET CREATE;T001;correo;yyyy-MM-dd` (prints `OK TICKET CREATED -> <precio>`), `TICKET DELETE;T001` (only when the ticket date is after the system date). Tickets reference their user, keep their purchase price, are persisted and kept as history.
- **BREAKING (user deletion rule)**: `USER DELETE` is rejected while the user has any ticket dated on or after the system date.
- **Group tickets**: `GROUPTICKET CREATE;TG01;G01;yyyy-MM-dd` generates one individual ticket per current member and applies a size discount (0% <3, 10% 3–5, 20% 6–10, 50% >10), printing `OK GROUP TICKET CREATED -> <precio>`. A group with a current/future group ticket is locked (no add/remove/delete). `GROUPTICKET DELETE;TG01` (future only) removes the group ticket and its internal tickets. Individual and group tickets share a common ticket abstraction.
- **Attractions**: `ATTRACTION CREATE;A01;Nombre;Capacidad`, `ATTRACTION DELETE;A01`, `ATTRACTION ENTER;A01;<ticket>`, `ATTRACTION EXIT;A01;<ticket>`, `ATTRACTION STATUS;A01`. Entry works polymorphically for individual and group tickets, requires a ticket for the system date, never exceeds capacity and is atomic for groups. Attractions are persisted; occupancy starts empty on every execution.
- **PlantUML model**: `uml/model.puml` documents the implemented classes, hierarchy, associations, compositions, navigability and multiplicities under the stated diagram conventions.
- **`--fly` mode**: `--fly` runs with all persistence files in a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted on termination; works in interactive and file modes; normal mode unchanged.
- `HELP` lists every new command.

## Capabilities

### New Capabilities
- `system-configuration`: The application-wide simulated date and the configurable individual ticket price.
- `user-groups`: Groups of existing users, membership rules, locking by active group tickets, and group persistence.
- `ticketing`: Individual tickets: purchase, deletion, history and persistence, and their effect on user deletion.
- `group-ticketing`: Group tickets composed of member tickets, discount brackets, group locking, and deletion.
- `attractions`: Attraction management, ticket-based entry/exit with capacity and date rules, and status queries.
- `design-documentation`: The PlantUML class model of the implementation and its conventions.
- `temporary-execution`: The `--fly` execution mode with isolated, self-deleting persistence.

### Modified Capabilities
- `user-management`: `USER DELETE` is additionally rejected while the user has a current or future ticket; a deleted user is removed from their groups.
- `command-interface`: `HELP` lists all commands added in this delivery.

## Impact

- New domain, controller and repository classes in `es.upm.parque`; `CommandProcessor` delegates to a new main controller.
- New data files under the data directory: `groups.txt`, `tickets.txt`, `group_tickets.txt`, `attractions.txt` (plus `users.txt`).
- New `uml/model.puml`; `tmp/` added to `.gitignore`.
- No new runtime dependencies.
