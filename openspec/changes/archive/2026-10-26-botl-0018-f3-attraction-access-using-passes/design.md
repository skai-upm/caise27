# Design

## Context

`AttractionManager.enter` resolves the ticket and calls a generic `enter(Attraction<T>, Admission)` that checks kind → date → users inside → capacity, then admits. `Attraction<T extends Admission>` has four subclasses. Passes live on `IndividualTicket` (`getSpeedPasses`, `getSpecialPass`); `PassManager.consume` marks and persists. `Bootstrap` initialises attractions before passes.

## Goals / Non-Goals

**Goals:**
- Pass rules expressed by the attraction types (polymorphism) and the admission interfaces, without `instanceof` on tickets.
- Validate everything first, mutate (enter + consume) last.

**Non-Goals:**
- Cascade deletion of passes (next proposal).

## Decisions

- **Admissions expose their individual tickets**: `Admission.getIndividualTickets()` — an `IndividualTicket` returns itself, a `GroupTicket` its composition. SpeedPass selection is "first unused SpeedPass of each individual ticket" and is identical for individual and group entries.
- **Attraction pass policy**: `Attraction.allowsSpeed()` (true by default; `SpecialAttraction` false) and `Attraction.requiredPasses(T admission)` returning the passes that must be consumed for a normal entry (empty by default; `SpecialAttraction` returns the ticket's Special Pass, after checking it exists and is unused). Errors are reported through a small `EntryCheck` result (message or passes) to keep the order of checks in one place.
- **Order of checks** in the generic helper: kind → SPEED allowed → date → users inside → capacity → passes (SpeedPasses for SPEED, Special Pass for SPECIAL) → enter → consume. Nothing is mutated before the last two steps, which gives atomicity.
- **Consumption** through `PassManager.consumeAll(List<PersonalPass>)` (marks all, persists once). `AttractionManager` receives `PassManager` by injection; `Bootstrap` initialises passes before attractions.
- **Command**: `ATTRACTION ENTER` accepts 3 or 4 parameters; the 4th must be `SPEED` (ignoring case).
- **Responses**: `OK ENTERED <n> USERS` plus ` (SPEEDPASS <id> CONSUMED)`, ` (<n> SPEEDPASSES CONSUMED)` or ` (SPECIAL PASS <id> CONSUMED)`.

## Risks / Trade-offs

- [A group whose members also hold extra SpeedPasses] → Exactly one per member is consumed, the first unused in creation order.
