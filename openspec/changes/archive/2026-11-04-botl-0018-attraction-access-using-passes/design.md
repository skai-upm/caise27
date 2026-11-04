# Design

## Context

`Attraction<T>.admit(access, today)` checks the generic bound and then `enter` validates date, visitors, presence and capacity before adding visitors. `PassManager` stores passes (with `consumed` flags) and persists every change with rollback. `ParkController.enterAttraction` resolves the attraction and the ticket and calls `admit`.

## Goals / Non-Goals

**Goals:** a strict preflight → commit sequence so that no failure path mutates occupancy or passes.

**Non-Goals:** new pass kinds, partial group entries, choosing which SpeedPass to use (the first unused one in purchase order is used).

## Decisions

- **Preflight in `Attraction`.** `checkAdmission(AttractionAccess, LocalDate)` performs the type check and every `enter` precondition without mutating; `admit` = `checkAdmission` + add visitors. `enter(T)` keeps its validation for typed callers.
- **Pass requirements decided by the controller** (the only component that sees both attractions and passes):
  1. `SPECIAL` + `SPEED` → `ERROR: Attraction X (SPECIAL) does not accept the SPEED option`.
  2. `attraction.checkAdmission(ticket, today)`.
  3. Passes to consume:
     - `SPEED`: the individual tickets of the access — the ticket itself if it is an `IndividualTicket`, or every part of a `GroupTicket`; for each, `passes.firstUnusedSpeedPass(t)` or `ERROR: Ticket <id> has no unused SpeedPass`.
     - `SPECIAL` (no option): `passes.unusedSpecialPass(ticket)` or `ERROR: Ticket <id> has no unused Special Pass`.
  4. Commit: `attraction.admit(ticket, today)` (cannot fail after step 2 within the single-threaded controller), then `passes.consume(list)`, which marks the passes and saves; if saving fails it restores the flags and throws, and the controller removes the just-admitted visitors so occupancy is unchanged.
- **Which SpeedPass.** The first unused SpeedPass of the ticket in purchase order (e.g. `SP1-1` before `SP1-2`).
- **Group members.** All parts of the group ticket count, matching "every individual ticket in the composition".
- **Output.** Normal entries keep `OK: N visitor(s) entered A with ticket T`. With consumption the line is followed by `; consumed SpeedPass SP1-1, SP2-1` or `; consumed Special Pass VIP1`.
- **Parsing.** `ATTRACTION ENTER` takes 2 or 3 parameters; the third must be `SPEED` (case-insensitive).
- **UML.** New dependency of `ParkController` on `PassManager` already exists; `Attraction` gains no fields; `SpecialAttraction` documented. No new classes, so only notes change.

## Risks / Trade-offs

- [Admission and pass save are two steps] → compensated by removing the admitted visitors if the pass save fails.
