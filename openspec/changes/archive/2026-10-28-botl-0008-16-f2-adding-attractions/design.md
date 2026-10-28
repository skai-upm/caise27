# Design

## Context

`ParkController` keeps users, groups and tickets; commands extend `ControllerCommand` which returns one success line. `ATTRACTION LIST` needs multi-line output.

## Goals / Non-Goals

**Goals:** attraction model and registry, ATTRACTION CREATE/LIST.

**Non-Goals:** entering/leaving with tickets (F2-C06/C07), persistence (F2-C08), attraction types (F3).

## Decisions

- **`Attraction`** (`model`): `identifier`, `name`, `capacity` (int), `List<User> occupants`; `getOccupants()` unmodifiable; `addOccupant(User)` rejects when full or already inside; `removeOccupant(User)`. Invariants live in the model.
- **Registry**: `Map<String, Attraction>` in `ParkController`, `createAttraction(id, name, capacityText)` parses capacity with a digit loop (no regex) and `Integer.parseInt`, rejecting ≤ 0 or overflow; `findAttraction`.
- **ATTRACTION LIST** returns a multi-line string (one e-mail per line) built from `getOccupants()`; `ControllerCommand.println` of a multi-line string is fine. Empty → `NO USERS IN ATTRACTION <id>`.

## Risks / Trade-offs

- [Occupant mutators unused by commands until F2-C06] → exercised by unit tests.
