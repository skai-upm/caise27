# Design

## Context

`ParkController` holds users, groups and tickets (repositories reset per execution). Command families live in small `*Commands` classes dispatched by `CommandProcessor`'s switch. There is no command to enter attractions yet, so occupants can only be added programmatically until the next milestone.

## Goals / Non-Goals

**Goals:**
- An attraction model that the access milestone can use directly (`enter`/`leave` with capacity).

**Non-Goals:**
- Access commands, capacity enforcement on entry, and persistence.

## Decisions

- **`model.Attraction`** has an id, a name, an `int capacity` and a `List<User> occupants`. It exposes an unmodifiable `getOccupants()` and package-level-agnostic `addOccupant`/`removeOccupant`/`isInside` so the next milestone can implement access. The `User` class is unchanged, which keeps the relationship one-way.
- **`repository.AttractionRepository`**: in-memory, id-keyed, insertion-ordered, owned by `ParkController` and reset in `startExecution`.
- **Capacity parsing** is digit-by-digit (no regex), consistent with the Delivery 1 restrictions. Values that overflow `int`, are 0, or contain any non-digit (sign, dot) are rejected.
- **`ATTRACTION LIST` output**: one multi-line response. The first line is `OK: attraction A01 (Dragon Rojo) has N user(s) inside:`, followed by one line per occupant email. When there are none, it is the single line `OK: attraction A01 (Dragon Rojo) has no users inside`.

## Risks / Trade-offs

- [Occupants only change programmatically in this milestone] → Covered by unit tests; the access milestone adds the commands.
