# Design

## Context

`Attraction<T>.enter(T, today, PassLookup)` checks date, `checkAccess` hook (Special Pass presence), duplicates and capacity, then admits. Passes have `consume()`. See proposal.md.

## Goals / Non-Goals

**Goals:** all checks first, then admission and consumption in one step; Special behavior stays in `SpecialAttraction`.
**Non-Goals:** persistence of consumption (F3-C09).

## Decisions

- **`EntryResult(List<User> users, List<Pass> consumed)`** record in the model.
- **`Attraction.enter(T, today, PassLookup, boolean speed)`**: date → `if (speed && !acceptsSpeedPasses())` reject → `passesToConsume(ticket, lookup, speed)`: default implementation picks, for every composition ticket, its first unconsumed SpeedPass when `speed` (rejecting if any is missing) → duplicates → capacity → admit all users → consume selected passes → result. Nothing is modified before the last checks pass.
- **`SpecialAttraction`**: `acceptsSpeedPasses()` = false; `passesToConsume` returns the ticket's unconsumed Special Pass (rejecting if none). Replaces the previous `checkAccess` hook.
- Old `enter(T, today, lookup)` kept as `speed = false` convenience returning users.
- Processor: `ATTRACTION ENTER` with 2 or 3 arguments; 3rd must be `SPEED`; prints users then consumed passes.

## Risks / Trade-offs

- None significant.
