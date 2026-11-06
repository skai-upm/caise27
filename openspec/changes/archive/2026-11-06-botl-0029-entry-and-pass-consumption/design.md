# Design

## Context

See proposal.md – Why. `Attraction<T extends Admission>` exposes `admissible`, `holdersOf(T)` and `missingRequirement(T, PassLookup)`; `AttractionService.admit` performs all checks before entering; passes are in memory with a `consume()` method; `ParkController.enterAttraction(attraction, ticket)` passes `PassService::passesOf` as `PassLookup`.

## Goals / Non-Goals

**Goals:**
- One admission algorithm; every check before any state change; consumption and entry applied together.

**Non-Goals:**
- Choosing a particular SpeedPass (any available one is fine; the first is used).

## Decisions

- **Attraction hooks**: `boolean acceptsSpeedPasses()` (true; `SpecialAttraction` false); `List<IndividualAccess> speedPassTickets(T)` — default derives from the capability (`GroupAccess` → composition, `IndividualAccess` → itself); `IndividualAttraction` returns the ticket and `GroupAttraction` its composition through their bounds; `List<Pass> automaticPasses(T, PassLookup)` (default none; `SpecialAttraction` returns its unconsumed Special Pass, which `missingRequirement` already guarantees exists).
- **`AttractionService.enter(attraction, ticket, today, passes, speed)`** returns `AccessResult(List<User> users, List<Pass> consumed)`: checks type, validity, special requirement, `SPEED` allowed, one unconsumed SpeedPass per required ticket (`ticket X has no unconsumed SpeedPass`), presence and capacity; only then enters all users and consumes the selected passes.
- **Command**: `ATTRACTION ENTER` takes 2–3 fields; option must be `SPEED` (`Error: invalid entry option FAST`). Output: `Entered A01: ana@correo.com` plus `; consumed SP01:1, PE01` when passes were consumed.
- **`ParkController.enterAttraction(attraction, ticket, speed)`** returns `AccessResult`; the 2-argument form remains for callers that only need the users.
- **Constituent tickets at entry** (found during apply): `TicketService.findForAccess(id)` falls back to `findIndividual(id)`, so a group-ticket constituent can be presented on its own as an individual ticket. Without this, a Special Pass bought for a constituent (allowed by section 9) could never be used, because group tickets cannot enter special attractions directly (section 12.3). This supersedes the Delivery 2 choice of accepting only top-level tickets.
