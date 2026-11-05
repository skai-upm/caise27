# Design

## Context

`AttractionCommands.enter` already computes entrants from `Ticket.getHolders()`, excludes users already inside, checks `entrants ≤ free` and then adds all of them. The only thing stopping group access is an explicit `instanceof GroupTicket` guard. `Attraction` provides `removeOccupant`, `isInside`, `getOccupants` and `getCapacity`.

## Goals / Non-Goals

**Goals:**
- Group access with no new branching, by deleting the guard.
- Exit and status computed solely from `Attraction` state.

**Non-Goals:**
- Occupancy persistence.

## Decisions

- **Remove the guard.** `getHolders()` of a `GroupTicket` returns the composition users, and the existing check-then-add sequence already provides the "indivisible" semantics, so no group-specific code is added.
- **`exit`**: look up the attraction, then the user by email in the park's users. If the email does not identify a current user, fall back to matching an occupant's email: a deleted user may still be inside. Then `removeOccupant`.
- **`status`**: a multi-line response. The first line is `OK: attraction A01 (Dragon Rojo) - capacity 20, inside 2, available 18`, followed by one line per occupant email. Free places are computed as `capacity - occupants.size()`.

## Risks / Trade-offs

- [Email changes via `USER UPDATE` while a user is inside] → Occupants are user references, so status shows the current email, and exit by the new email works.
