# Design

## Context

`Park` holds users, groups and tickets repositories; commands are registered in `CommandProcessor`.

## Goals / Non-Goals

**Goals:** attraction model ready for entry operations (capacity checks, atomic multi-user entry). **Non-Goals:** entry/exit commands, persistence.

## Decisions

- **`model.Attraction`**: final identifier, name, final capacity, ordered occupant list; `availableCapacity()`, `canAdmit(int n)`, `admitAll(Collection<User>)` (all-or-nothing; used by F2-C06/C07), `contains(User)`, read-only `getOccupants()`. Adding the mutators now keeps the capacity invariant inside the model.
- **`AttractionRepository`** (insertion-ordered map) via `Park.attractions()`, reset by `Park.start`.
- **`AttractionService`** validates creation (capacity parsed with `Integer.parseInt` after a digits-only check, > 0) and builds the list text.
- **List format**: `ATTRACTION A01 (Dragon Rojo) 2/20:` followed by one email per line; empty: `ATTRACTION A01 (Dragon Rojo) 0/20: no users`.

## Risks / Trade-offs

- [Occupants are not persisted until F2-C08] → expected.
