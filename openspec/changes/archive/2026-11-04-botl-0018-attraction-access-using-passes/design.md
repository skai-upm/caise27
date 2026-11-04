# Design

## Context

`Attraction<T extends Admission>.admit(Admission, today)` validates type, date, presence and capacity, then (through the `checkAdditionalConditions` hook) admits atomically; it throws before mutating. `PassService` holds passes (`SpeedPass`, `SpecialPass`) with `consume()` and saves on `save()`. `ParkController.enterAttraction(attractionId, ticketId)` resolves both and calls `admit`.

## Goals / Non-Goals

**Goals:** pass-aware entry with all-or-nothing semantics; model classes stay free of service dependencies.

**Non-Goals:** passes affecting exits, priorities/queues, partial group entries.

## Decisions

- **Selection before mutation** (`PassService`):
  - `individualTicketsOf(Admission)`: the ticket itself for `IndividualAccess`, the constituents for `GroupAccess`.
  - `selectSpeedPasses(Admission)`: for each individual ticket, the first unconsumed `SpeedPass` in purchase order; throws if any ticket has none. No state change.
  - `selectSpecialPass(IndividualTicket)`: the ticket's `SpecialPass`, throwing if missing or consumed.
  - `consume(List<Pass>)`: marks them consumed and saves; on save failure the flags are reverted and the error propagates.
- **Controller flow** `enterAttraction(attractionId, ticketId, boolean speed)`:
  1. resolve attraction and ticket;
  2. if SPECIAL: reject `speed`; require the ticket to be accepted (individual) and select its Special Pass;
  3. else if `speed`: select SpeedPasses;
  4. `attraction.admit(ticket, currentDate)` (all standard checks; throws without mutating);
  5. consume the selected passes; if that fails, the visitors are removed again (`attraction.exit`) so the operation stays atomic.
  Returns `EntryResult(int entered, List<Pass> consumed)`.
- **Presentation**: `ATTRACTION ENTER` accepts 2 or 3 parameters (third must be `SPEED`); response `OK: N user(s) entered attraction X` plus `; SpeedPass consumed: …` / `; SpeedPasses consumed: a, b` / `; Special Pass consumed: S01` when passes were consumed.
- **Model**: new record `EntryResult` in `business.controller`.

## Risks / Trade-offs

- [Selection is checked before `admit`, so an invalid ticket type at SPECIAL might report the pass problem first] → for SPECIAL the type check (`accepts`) is done before pass selection to report the more relevant error.
