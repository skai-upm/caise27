# Design

## Context

`ParkController` centralizes users, groups and tickets. See proposal.md for the flagged absence of entry/exit.

## Goals / Non-Goals

**Goals:** `Attraction` model, ATTRACTION CREATE, ATTRACTION LIST.

**Non-Goals:** ATTRACTION ENTER/EXIT/STATUS, ticket validation for access, persistence.

## Decisions

- **`Attraction`**: private final `identifier`, `name`, `int capacity`; private `List<User> occupants` (`ArrayList`, keeps entry order). `addOccupant(User)` returns false when full or already inside; `removeOccupant(User)`; `getOccupants()` unmodifiable; `isFull()`. `User` is untouched (no attraction reference).
- **Registry**: `LinkedHashMap<String, Attraction>` in `ParkController`, `createAttraction(id, name, capacityText)` and `findAttraction`, `existingAttraction` for errors; cleared by `reset()`.
- **Capacity parsing** without regular expressions: every char a digit, then `Integer.parseInt` (overflow → error), must be > 0.
- **LIST** (read-only): processor prints each occupant email on its own line or `NO USERS IN ATTRACTION <id>`.

## Risks / Trade-offs

- [`addOccupant` exists before ENTER] → it only enforces the invariants stated here (capacity, no duplicates); ticket rules come with ENTER.
