# Proposal

## Why

Users, groups and tickets exist, but there is nothing to use the tickets on. Attractions with a capacity, entry by individual or group ticket on the ticket's day, exit, and a live status complete the visitor flow. They also exercise polymorphism over the ticket hierarchy.

## What Changes

- New `Attraction` entity: identifier, name, maximum capacity, and the collection of users currently present.
- `ATTRACTION CREATE;A01;Tornado;20` creates an attraction. Attraction definitions persist across runs; they always start empty, as if the day were starting.
- `ATTRACTION ENTER;A01;T001` / `ATTRACTION ENTER;A01;TG01` admits the ticket's user or all users of the group ticket's individual tickets. The ticket must be dated exactly on the system date. Capacity can never be exceeded, and group entry is atomic (all or none). Both ticket kinds are handled uniformly through the `Ticket` abstraction.
- `ATTRACTION EXIT;A01;T001` removes the users of that ticket from the attraction and frees their places. The date is not checked on exit.
- `ATTRACTION STATUS;A01` shows identifier, name, maximum capacity, number of users present, available places and the users present, regardless of how they entered.
- `HELP` and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `attractions`: attraction entity, `ATTRACTION CREATE`, and the stored attraction definitions.
- `attraction-access`: `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, capacity and date rules.

### Modified Capabilities
- `application-control`: `HELP` lists the four `ATTRACTION` commands.
- `domain-model-diagram`: the diagram also shows attractions and the polymorphic ticket access.

## Impact

- New classes `Attraction`, `AttractionManager`, `AttractionFileRepository`. `Ticket` gains the abstract `getVisitors()`, implemented by `IndividualTicket` and `GroupTicket`. `TicketManager.findAnyTicket` resolves individual, group and constituent ids. `CommandProcessor` dispatches the commands, and `App` wires `attractions.txt` (git-ignored).
- Existing files and commands are unchanged. No new dependencies.
- Out of scope: attraction types, passes, reservations.
