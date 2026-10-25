# Proposal

## Why

Delivery 2 of the Amusement Park turns the user-management base into a working park model: time-dependent rules need a controllable simulated date, users must be organised into groups, individual and group tickets must be sold (with group discounts and lock rules), attractions must admit visitors with those tickets, the object model must be documented in PlantUML, and isolated test runs need a temporary persistence mode that leaves real data untouched.

## What Changes

- `SYSTEM DATE;yyyy-MM-dd` sets the single application-wide current date (initialised from the OS date at startup, not persisted); all time-dependent rules use it. `SYSTEM PRICE;<amount>` changes the individual ticket price (initialised to 40.00 each execution, not persisted).
- Groups: `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`. Groups reference existing users (no copies), reject duplicates and unknown users, and persist across runs.
- Individual tickets: `TICKET CREATE;Id;Correo;Fecha` (reply `OK TICKET CREATED -> <Precio>`), `TICKET DELETE;Id` (only for dates after the system date). Tickets reference their user, keep their price, persist, and remain as history once past.
- Group tickets: `GROUPTICKET CREATE;Id;Grupo;Fecha` (reply `OK GROUP TICKET CREATED -> <Precio>`) generates one individual ticket per current member and applies the 0/10/20/50 % discount brackets; `GROUPTICKET DELETE;Id` removes it and its internal tickets when its date is in the future. A group with a current/future group ticket is locked.
- **BREAKING** (rule change): `USER DELETE` is rejected while the user has any ticket dated on or after the system date; deleting a user also removes them from their groups.
- Attractions: `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;Ticket` (individual or group ticket, valid only on the system date, atomic group entry, capacity never exceeded), `ATTRACTION EXIT;Id;Ticket`, `ATTRACTION STATUS;Id`. Attractions persist (definition only) and start empty each run.
- `uml/model.puml`: PlantUML class diagram documenting the implemented design.
- `--fly` option: all persistence files live in a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the run ends; works with interactive and file modes.
- `HELP` lists every new command.

## Capabilities

### New Capabilities
- `system-configuration`: Simulated current date and individual ticket price set through SYSTEM commands.
- `user-groups`: Creation, membership management, deletion and persistence of user groups.
- `ticketing`: Individual and group tickets, pricing and discounts, deletion rules, group locking and ticket persistence.
- `attractions`: Attraction management, ticket-based entry/exit with capacity rules, and status queries.
- `design-model`: PlantUML documentation of the implemented class model.
- `temporary-execution`: `--fly` isolated execution with temporary persistence files.

### Modified Capabilities
- `user-management`: Delete user now blocked by current/future tickets and removes group memberships.
- `command-processing`: HELP lists the new commands; interactive/file mode selection accounts for the `--fly` option.

## Impact

- New domain, manager and repository classes in `es.upm.parque`; `CommandProcessor` dispatches through a main controller holding the date and price.
- New data files under `data/` (`groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`); `tmp/` is added to `.gitignore`.
- New `uml/model.puml`. No new dependencies.
