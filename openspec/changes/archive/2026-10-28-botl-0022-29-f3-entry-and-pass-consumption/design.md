# Design

## Context

`Attraction<T extends Admission>` has the runtime gate `enterWith(Admission, passes)`, typed `enter(T, passes)` and the hook `checkEntry(T, passes)` (used by `SpecialAttraction` to require a Special Pass). `admit(List<User>)` validates capacity/duplicates before mutating. The controller passes the passes of the ticket's individual tickets.

## Decisions

- **Hook replaced** by `protected List<Pass> passesToConsume(T admission, Collection<Pass> passes, boolean speed)`: validates pass conditions and returns the passes to consume without modifying them. Base implementation: no passes when `speed` is false; otherwise the first unconsumed `SpeedPass` of each individual ticket, rejecting if one is missing. `SpecialAttraction` overrides: `speed` → reject; otherwise its unconsumed `SpecialPass` → `List.of(pass)` or reject.
- **Atomic `enter(T, passes, speed)`**: `passesToConsume` → `admit(users)` (throws before mutating if capacity/duplicates fail) → `consume()` each selected pass. Pass consumption cannot fail, so either everything happens or nothing does. Returns an `EntryResult(users, consumedPasses)` record.
- **Controller**: `enterAttraction(attraction, ticket)` keeps its signature (normal entry); new overload with `boolean speed` returns `EntryResult`. Date check stays in the controller before the attraction call.
- **Command**: `ATTRACTION ENTER` accepts 2–3 fields; field 3 must equal `SPEED`. Output: existing `ATTRACTION ENTERED: <n> user(s) in <id>` line plus `SPEEDPASS CONSUMED: <id>` / `SPECIALPASS CONSUMED: <id>` lines.
