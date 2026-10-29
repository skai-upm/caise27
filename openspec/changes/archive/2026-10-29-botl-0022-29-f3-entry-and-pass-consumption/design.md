# Design

## Context

`Attraction<T>.enter(T, today, PassLookup)` checks date, `checkAdmission`, duplicates and capacity, then adds occupants; `SpecialAttraction.checkAdmission` requires an unconsumed Special Pass (not consumed). Passes live in `ParkController`.

## Goals / Non-Goals

**Goals:** SPEED option, Special Pass consumption, atomic entry + consumption. **Non-Goals:** invoices of passes.

## Decisions

- **`AccessTicket.individualTickets()`**: the individual tickets whose holders enter (`IndividualTicket` → itself; `GroupTicket` → composition).
- **`PassLookup`** becomes `Optional<Pass> unconsumedSpeedPass(IndividualTicket)` and `Optional<Pass> unconsumedSpecialPass(IndividualTicket)`; `ParkController` implements the lookups over its registry (purchase order).
- **`Entry`** record (`List<User> entrants`, `List<Pass> consumed`) returned by `Attraction.enter(T ticket, LocalDate today, PassLookup passes, boolean speed)`.
- **Template**: (1) date; (2) `passesToConsume(ticket, passes, speed)` — default: if `speed`, one unconsumed SpeedPass per individual ticket or reject, else none; `SpecialAttraction` overrides: reject `speed`, require the unconsumed Special Pass; (3) duplicates; (4) capacity; then (5) add occupants and `consume()` the selected passes. Every check precedes every mutation.
- **Presentation**: ATTRACTION ENTER with 2 arguments, or 3 when the third is `SPEED`; prints entrants and consumed passes.

## Risks / Trade-offs

- [PassLookup signature change] → only `ParkController` implements it.
