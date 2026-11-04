# Design

## Context

- `ParkController.enterAttraction(aid, tid)` resolves both objects and calls `Attraction.enterAny`, which checks the ticket kind, then date, holders, already-present and capacity before mutating occupancy.
- `PassManager` holds passes with a `consumed` flag and `passesFor(ticket)`.
- `SpecialAttraction` is `Attraction<IndividualAccess>`.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- All-or-nothing entry with pass consumption, with no change to the non-pass path.

**Non-Goals:**
- Refunding consumed passes, choosing a specific pass by id, SPEED at SPECIAL attractions.

## Decisions

- **Orchestration in `ParkController.enterAttraction(aid, tid, boolean speed)`**, returning `EntryResult(attraction, admitted, List<Pass> consumed)`:
  1. Resolve the attraction and ticket. If `!attraction.accepts(ticket)`, reject with the existing type message.
  2. Determine the individual tickets involved: the ticket itself, or the constituents when the ticket is a `GroupAccess`.
  3. If the attraction is `SPECIAL`: reject `speed`, then select the ticket's unconsumed `SpecialPass` (error if none).
  4. Else, if `speed`: select the first unconsumed `SpeedPass` of each involved ticket, failing on the first ticket without one. Selection does not mutate anything.
  5. Call `attraction.enterAny(ticket, today)`; any failure propagates before passes are touched.
  6. Call `passes.consume(selected)`, which marks them consumed and saves. If saving throws, the marks are reverted and `attraction.exitAny(ticket)` undoes the admission before the error propagates.
- **Selection order:** the first unconsumed SpeedPass in purchase order, so consumption is deterministic.
- **`PassManager.consume(List<Pass>)`:** marks all passes, saves once, and unmarks all on failure (`Pass.markUnconsumed()`, package-level use only by the manager through a public method documented for rollback).
- **Command parsing:** `ATTRACTION ENTER` accepts 2 or 3 parameters. The third must equal `SPEED` (case-insensitive); otherwise `ERROR: invalid entry option`.
- **Reply:** `OK: <n> user(s) entered attraction <id> (<free> places available)`, then `; SpeedPass consumed: SP1` / `; SpeedPasses consumed: P1, P2` / `; Special Pass consumed: SX1` when applicable.

## Risks / Trade-offs

- [SPECIAL logic lives in the controller rather than in `SpecialAttraction`] → The model has no access to passes (layering), so the controller coordinates. `SpecialAttraction`'s generic bound still guarantees an individual ticket at the typed API.
