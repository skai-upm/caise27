# Design

## Context

`Attraction<T>.enter(TicketAccess)` checks the bound, type hooks, already-inside and capacity, then admits. Passes reference their tickets and are found through `PassRepository`.

## Goals / Non-Goals

**Goals:** indivisible entry + consumption on the existing generic gate; model independent of repositories. **Non-Goals:** choosing a specific SpeedPass (any available one is used).

## Decisions

- **`PassLookup` interface (model)**: `Optional<Pass> availableSpeedPass(IndividualTicket)`, `Optional<Pass> specialPass(IndividualTicket)`; `AttractionService` implements it over the `PassRepository`. Keeps the model free of persistence.
- **`TicketAccess.individualTickets()`**: an individual ticket returns itself, a group ticket its composition — used to require one SpeedPass per individual ticket without type checks.
- **`Attraction.enter(ticket, speed, lookup)` → `EntryResult(users, consumedPasses)`**: (1) bound check, (2) `speed && !acceptsSpeedPasses()` → reject, (3) `checkEntry`, (4) collect passes: one available SpeedPass per individual ticket when `speed`, plus `requiredPasses(T, lookup)` (SPECIAL: its unconsumed Special Pass), (5) already-inside/capacity, (6) admit, (7) consume collected passes. All checks precede steps 6–7, so failures change nothing.
- **`SpecialAttraction`** overrides `acceptsSpeedPasses()` (false) and `requiredPasses` (Special Pass rule) — the difference from INDIVIDUAL is behavior, not a ticket hierarchy.
- **Command**: `ATTRACTION ENTER` takes 3 or 4 elements; output lists consumed passes, e.g. `ENTRY GRANTED: 1 user(s) entered A01. SpeedPass consumed: SP01:1` and `... Special Pass consumed: PE01`. `PassRepository.update()` is called after consumption (persisted in F3-C09).

## Risks / Trade-offs

- [Consumed SpeedPass choice is arbitrary] → the first available in purchase order; reported in the output.
