# Design

## Context

`AttractionService.enter` already works on `Ticket.getHolders()` and checks date, already-inside and capacity before calling `attraction.enter(holders)`; it currently rejects `GroupTicket`. `GroupTicket.getHolders()` returns the users of its composition.

## Goals / Non-Goals

**Goals:**
- Group access via the same path; exit; status.

**Non-Goals:**
- Persistence of occupants (never persisted, per F2-C08).

## Decisions

- **Remove the group guard:** all checks run on the full holder list before `attraction.enter(holders)` adds them in one call, so access is all-or-nothing without a separate group mechanism.
- **Exit by email:** `AttractionService.exit(attractionId, email)` finds the occupant with that email among the attraction's users (occupants are users, not tickets), then `attraction.exit(user)`.
- **Status output:**
  ```
  ATTRACTION A01 - Dragon Rojo
  Capacity: 20
  Users inside: 3
  Available places: 17
  Inside: ana@correo.com, pedro@correo.com, luis@correo.com
  ```
  (`Inside: none` when empty). Computed only from `Attraction`.
- **Output:** `EXITED ATTRACTION A01: ana@correo.com`.
- **Mandatory commit:** `[F2-C07] Add group attraction access` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Email of an occupant changed by USER UPDATE while inside] → Exit uses the user's current email, since the attraction holds the user object.
