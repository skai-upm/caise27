# Design

## Context

`Attraction<T extends AccessTicket>.enter(AccessTicket, today)` checks the kind at run time and `admit(T, today)` checks date, presence and capacity and adds the users. Passes are managed by `PassController` (consumed flag, persisted). `ParkController.enter(attractionId, ticketId)` coordinates tickets and attractions. See proposal.md.

## Goals / Non-Goals

**Goals:** pass-aware entry with a strict check-then-commit sequence; persistence of consumption.

**Non-Goals:** consuming passes on exit, choosing a specific pass by id.

## Decisions

- **Preflight in the attraction:** `requireAdmissible(AccessTicket, today)` performs every attraction check (kind, date equals system date, no visitor already present, capacity) without mutation; `admit`/`enter` reuse it, so the logic stays in one place.
- **Type hooks:** `Attraction.acceptsSpeed()` (true by default, false in `SpecialAttraction`) and `requiresSpecialPass()` (false by default, true in `SpecialAttraction`). SPECIAL already rejects group tickets through its `IndividualAccess` bound.
- **Sequence in `ParkController.enter(attractionId, ticketId, speed)`:** 1) resolve ticket and attraction; 2) reject `SPEED` where not accepted; 3) `requireAdmissible`; 4) select passes: with `SPEED`, for each individual ticket of the entry (the ticket itself, or every constituent of a group ticket) the earliest unconsumed `SpeedPass`, failing on the first ticket without one; at SPECIAL, the ticket's unconsumed `SpecialPass`; 5) commit: `attraction.enter`, then `passes.consume(selected)`, which marks and saves them; if saving fails the marks are undone and the admitted users are removed again, so no partial state remains.
- **Command:** `ATTRACTION ENTER` accepts 3 or 4 tokens; the 4th must be `SPEED` (case-insensitive), otherwise an error. Response: unchanged text for normal entries, plus `; consumed SpeedPass S1-1` / `; consumed SpeedPasses TG01-a, ...` / `; consumed Special Pass SP1` when passes are used.

## Risks / Trade-offs

- [Pass chosen automatically] → earliest bought first; deterministic and sufficient for the source.
