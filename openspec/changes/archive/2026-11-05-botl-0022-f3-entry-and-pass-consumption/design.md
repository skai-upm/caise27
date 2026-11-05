# Design

## Context

`Attraction<T extends GeneralAccess>.admissionFor` checks the ticket kind and the special-pass presence. `ParkController.enterAttraction` checks date, duplicates and capacity, then admits holders. Passes hold `consumed` and are found through `PassLookup`. See proposal.md - Why.

## Decisions

- **`GeneralAccess.individualTicketIds()`** returns the ticket itself for individual tickets and the components for group tickets. SpeedPass checks then work the same for both kinds, without branching on concrete types.
- **`Attraction.admit(GeneralAccess, PassLookup, boolean speed)`** returns `Admission(List<String> holders, List<Pass> toConsume)`:
  1. Check the kind and cast to `T`.
  2. If `speed`, require `acceptsSpeedPasses()` (overridden as `false` by `SpecialAttraction`), then pick one unconsumed `SpeedPass` per individual ticket id. A missing one rejects the entry.
  3. Add `requiredPasses(T, passes)`. `SpecialAttraction` returns its unconsumed Special Pass or rejects.

  This replaces `admissionFor`/`checkAdmission`, so type-specific behavior stays in the attraction subclass and is typed by its bound.
- **Two-phase entry in `ParkController`**: validate (existence, date, admission, duplicates, capacity), then commit (enter every holder, mark every `toConsume` pass as consumed). No state changes before the commit phase, which makes the operation indivisible.
- **Output**: `ENTERED ATTRACTION <id>: <emails>`, followed by `CONSUMED PASS: <Type> <passId>` per consumed pass.
