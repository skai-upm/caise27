# Design

## Context

`Attraction<T>.admit(ticket, today, EntryRules)` checks kind, validity, the type-specific `checkEntry`, presence and capacity, then enters the users. `EntryRules` only answers `hasUnconsumedSpecialPass`. Passes live in the controller, and `Pass.consume()` exists. Passes are persisted with their consumed flag.

## Goals / Non-Goals

**Goals:** a SPEED option that works for every ticket kind through the capability interfaces, all-or-nothing entry plus consumption, and visible consumed-pass ids.
**Non-Goals:** choosing among SpeedPasses by any rule other than "first available".

## Decisions

- **`AccessTicket.individualTickets()`** (`List<? extends IndividualAccess>`): an individual ticket returns itself, and a group ticket returns its composition. SpeedPass requirements are then uniform ("each individual ticket involved").
- **`EntryRules`** becomes the pass port that attractions use during admission:
  - `Optional<String> availableSpeedPass(IndividualAccess)` and `Optional<String> availableSpecialPass(IndividualAccess)` return the first unconsumed pass id.
  - `void consume(String passId)`.
  The controller implements it over its pass map (insertion order, so the choice is deterministic).
- **`Attraction.admit(ticket, today, rules, speed)`** returns an `EntryResult(entrants, consumedPasses)`. Order of work:
  1. Check kind, then `speed && !acceptsSpeedPasses()`, then validity, then `checkEntry` (type rule), then presence and capacity.
  2. If `speed`, collect one available SpeedPass per individual ticket and fail if any is missing.
  3. Collect type-specific passes to consume through the `passesToConsume(T, rules)` hook (SPECIAL returns its Special Pass).
  4. Only now enter all users and consume all collected passes.
  Steps 1-3 never mutate, which gives atomicity.
- **`SpecialAttraction`**: `acceptsSpeedPasses()` is false, `checkEntry` requires an available Special Pass, and `passesToConsume` returns it.
- **Controller** `enterAttraction(attractionId, ticketId, speed)` calls `save()` when passes were consumed, because occupants are not persisted but pass status is.
- **Output**: `ENTERED A01: ana@correo.com (CONSUMED: SP01:1)`, with the consumed list omitted when empty.

## Risks / Trade-offs

- [Pass lookup is a linear scan per ticket] → Pass counts are small, and simplicity wins.
