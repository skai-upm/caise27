# Proposal

## Why

Users, groups and tickets exist but visitors cannot use their tickets anywhere. The park needs attractions with a capacity that visitors enter with an individual or group ticket valid for the current day, leave, and whose occupancy can be queried at any moment.

## What Changes

- New commands: `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;IdTicket`, `ATTRACTION EXIT;Id;IdTicket`, `ATTRACTION STATUS;Id`.
- An attraction has an identifier, a name, a maximum capacity and the collection of users currently present.
- Entering with an individual ticket adds its user; with a group ticket adds the users of all its internal tickets. Both are handled uniformly through a common admission abstraction implemented by every ticket type.
- Entry requires a ticket dated exactly on the system date; capacity can never be exceeded; group entry is atomic (all or none); a user already present cannot enter again.
- Exiting with a ticket removes its users present in the attraction and frees capacity (no date check).
- `ATTRACTION STATUS` shows identifier, name, capacity, current occupancy, available places and the users present, without distinguishing how they entered.
- Attraction definitions persist between executions; occupancy does not (attractions start empty, as at the start of a day).
- Deleted users are removed from any attraction. HELP and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `attraction-management`: Attraction definition and persistence, ticket-based entry and exit rules, capacity, and status query.

### Modified Capabilities
- `command-processing`: HELP lists the ATTRACTION commands.
- `user-management`: deleting a user also removes it from attractions.

## Impact

- New interface `Admission` (implemented by `Ticket`), new classes `Attraction`, `AttractionManager`, `AttractionStorage`; `TicketManager` gains lookup of internal tickets; `CommandProcessor` routes `ATTRACTION`.
- New data file `data/attractions.txt`; `docs/uml/model.puml` updated.
