# Proposal

## Why

Delivery 2 closes by bringing visitors into the park: attractions with limited capacity that users enter with their individual or group tickets. Operators also need to see who is currently inside each attraction.

## What Changes

- New commands: `ATTRACTION CREATE;Id;Nombre;Capacidad`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;IdAtraccion;IdEntrada`, `ATTRACTION EXIT;IdAtraccion;IdEntrada`, `ATTRACTION STATUS;IdAtraccion`, all listed in `HELP`.
- Entry admits the users of an individual ticket or of all individual tickets inside a group ticket, handled uniformly through the common ticket abstraction.
- Entry requires a ticket dated exactly on the system date and never exceeds capacity; group entry is atomic (all or none).
- Exit frees the places of the ticket's users; dates are not checked on exit.
- Status shows identifier, name, capacity, current occupancy, available places and present users.
- Attractions (id, name, capacity) are persisted; occupancy is not — attractions start empty on every run.
- PlantUML model updated.

## Capabilities

### New Capabilities
- `attractions`: Attraction management, ticket-based entry/exit with capacity and date rules, and status queries.

### Modified Capabilities
- `uml-model`: the model must show attractions and their present users.

## Impact

- New classes `Attraction`, `AttractionRepository`, `AttractionManager`; `CommandProcessor`, `App`, `uml/model.puml` updated.
- New runtime file `data/attractions.txt`.
