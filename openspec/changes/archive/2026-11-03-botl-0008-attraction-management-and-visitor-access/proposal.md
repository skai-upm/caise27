# Proposal

## Why

Users, groups and tickets exist, but there is nothing to use them for inside the park. Attractions with a capacity, ticket-based entry for individuals and whole groups, exits and a live status query integrate all previous features.

## What Changes

- New `Attraction` (identifier, name, maximum capacity, collection of users currently present) and `ATTRACTION CREATE;A01;Tornado;20`; attraction definitions persisted in `attractions.txt`, always empty at startup.
- `ATTRACTION ENTER;A01;T001` / `ATTRACTION ENTER;A01;TG01`: entry with an individual or a group ticket valid for the system date, handled uniformly through the ticket abstraction; capacity never exceeded; group entry atomic.
- `ATTRACTION EXIT;A01;T001`: the users of that ticket leave and free their places (no date check). The email of a present user is also accepted (see design: open question on the EXIT contract).
- `ATTRACTION STATUS;A01`: identifier, name, maximum capacity, users present, available places and the list of present users.
- HELP and `uml/model.puml` updated.
- No attraction types, passes or reservations.

## Capabilities

### New Capabilities
- `attraction-catalog`: attraction creation, its data and persistence of definitions.
- `attraction-access`: entering with individual or group tickets and leaving attractions.
- `attraction-status`: the `ATTRACTION STATUS` query.

### Modified Capabilities
- `application-control`: HELP lists the four ATTRACTION commands.
- `domain-model-diagram`: the diagram also represents attractions and the polymorphic ticket access.

## Impact

- New `Attraction`, `AttractionService`, `AttractionFileStorage`; `ParkTicket.getVisitors()` implemented by `Ticket` and `GroupTicket`; `TicketService.findUsable`; `CommandProcessor` commands; `App` wiring; `uml/model.puml`.
- New runtime file `attractions.txt` (git-ignored). Existing file formats unchanged.
