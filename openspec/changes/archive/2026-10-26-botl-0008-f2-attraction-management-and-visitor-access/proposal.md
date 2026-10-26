# Proposal

## Why

The park's attractions are the last Delivery 2 element: users, groups and tickets must come together when visitors enter rides with their individual or group tickets, under capacity limits, and staff must be able to see each attraction's current state.

## What Changes

- `ATTRACTION CREATE;<id>;<name>;<capacity>` creates an attraction; `ATTRACTION DELETE;<id>` removes an empty one.
- `ATTRACTION ENTER;<attractionId>;<ticketId>` admits the ticket's users: the ticket's user for an individual ticket, or the users of all individual tickets composing a group ticket. Both kinds are handled uniformly through the ticket abstraction.
- Entry requires the ticket to be for the current system date and enough free capacity for all its users; group entry is atomic (all or none).
- `ATTRACTION EXIT;<attractionId>;<ticketId>` removes the ticket's users from the attraction and frees their places (no date check).
- `ATTRACTION STATUS;<id>` shows identifier, name, capacity, current occupancy, available places and the users present.
- Attractions (id, name, capacity) persist between executions; they always start empty.
- The PlantUML model is updated; HELP lists the new commands.

## Capabilities

### New Capabilities
- `attractions`: attraction lifecycle, ticket-based entry/exit with capacity rules, and status query.

### Modified Capabilities
<!-- None -->

## Impact

- New `Attraction`, `AttractionManager`, `AttractionRepository`; `Ticket` gains a polymorphic `getHolders()`; `CommandProcessor`, `App`, `uml/model.puml` updated.
- New data file `data/attractions.txt`.
