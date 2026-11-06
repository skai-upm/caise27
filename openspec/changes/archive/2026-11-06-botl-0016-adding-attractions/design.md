# Design

## Context

See proposal.md – Why. `ParkController` owns per-execution services (users, groups, tickets) and is the single access point.

## Goals / Non-Goals

**Goals:**
- Attraction state (occupants) owned by the attraction; read-only listing.

**Non-Goals:**
- Entering/exiting logic (F2-C06/07) beyond the model operations it will need.

## Decisions

- **`Attraction` (`es.upm.park.model`)**: final `id`, `name`, `capacity`; `List<User> occupants` with `getOccupants()` (unmodifiable), `isFull()`, `contains(User)`, `enter(User)`, `leave(User)` — the last three prepared for access milestones and unit-tested now.
- **Capacity parsing** without regex: all ASCII digits, then `Integer.parseInt` guarded against overflow (length ≤ 9), must be > 0.
- **`AttractionService(AttractionRepository)`**: `create`, `find`, `occupantEmails(id)`.
- **Output of `ATTRACTION LIST`**: `Users in A01:` followed by one email per line; empty → `Attraction A01 has no users inside`.
- **Messages**: `Attraction created: A01`; errors `attraction A01 already exists`, `invalid capacity <v>`, `attraction name is required`, `no attraction A01`.
