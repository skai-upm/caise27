# Proposal

## Why

Users, groups and tickets exist, but there is nothing to use the tickets on. The park needs attractions with a limited capacity, which visitors enter using individual or group tickets valid for the configured date, plus exits and a status query that shows the real occupancy.

## What Changes

- New `Attraction` with identifier, name, maximum capacity and the collection of users currently present.
- `ATTRACTION CREATE;Id;Nombre;Capacidad` (e.g. `ATTRACTION CREATE;A01;Tornado;20`) creates an attraction. Attraction definitions persist in `attractions.txt`; every execution starts with all attractions empty.
- `ATTRACTION ENTER;IdAtraccion;IdTicket` admits the holders of an individual ticket (its owner) or a group ticket (the owners of its constituent tickets):
  - the ticket must be dated exactly on the `SYSTEM DATE`;
  - capacity can never be exceeded;
  - group entry is atomic.
- `ATTRACTION EXIT;IdAtraccion;IdTicket` removes the ticket's holders who are present and frees their places. No date check applies.
- `ATTRACTION STATUS;IdAtraccion` prints the identifier, name, maximum capacity, number present, available places and the users present, without distinguishing individual from group entries.
- Access is handled uniformly through polymorphism: abstract `Ticket.getHolders()` is implemented by `IndividualTicket` and `GroupTicket`, so `Attraction` has a single entry path.
- `HELP` and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `attractions`: Attraction definition, creation, persistence of definitions and the status query.
- `attraction-access`: Ticket-based entry and exit, date validity, capacity and atomic group entry.

### Modified Capabilities
- `application-control`: "HELP command" lists the attraction commands.
- `uml-model`: "PlantUML class model" includes attractions and the polymorphic ticket holders.

## Impact

- New: `Attraction`, `AttractionManager`, `AttractionFileStorage`, tests.
- Changed: `Ticket`/`IndividualTicket`/`GroupTicket` (`getHolders()`), `TicketManager` (`findAnyTicket`), `CommandProcessor`, `App` (`attractions.txt`), PlantUML.
- Out of scope: attraction types, passes, reservations.
