# Design

## Context

`Attraction<T>` performs atomic, typed entry (`enter` → type check → `admit`: date, presence, `checkAdditional`, capacity). `PassManager` holds passes (`Pass.consume()`, persisted in `passes.txt`). `ParkController.enterAttraction(aid, tid)` delegates to `AttractionManager.enter`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** consume passes only after a fully successful entry; atomic groups; polymorphic attraction rules.

**Non-Goals:** consuming passes on exit, cascade deletion (next change).

## Decisions

- **Polymorphic rules on attractions**: `Attraction.allowsSpeed()` (default true) and `requiresSpecialPass()` (default false); `SpecialAttraction` overrides both. The controller never switches on the type enum.
- **Orchestration in `ParkController.enterAttraction(aid, tid, speed)`** (it already coordinates cross-entity rules):
  1. resolve attraction and credential (existing errors);
  2. `speed && !allowsSpeed()` → reject;
  3. collect required passes without consuming: for `speed`, `PassManager.findAvailableSpeedPass(t)` for each individual ticket of the credential (`IndividualTicket` itself, or `GroupTicket.getTickets()`); for `requiresSpecialPass()` and an individual ticket, `findAvailableSpecialPass(t)`; any missing → reject;
  4. `attraction.enter(credential, today)` (type/date/presence/capacity; atomic);
  5. on success, `PassManager.consume(passes)` (marks, saves; on save failure restores the marks) — if it fails, the entry is undone with `attraction.exit(credential)` and an error is returned;
  6. output: the entry message plus ` SpeedPasses consumed: SP01-1.` / ` Special Pass consumed: SPC1.`.
  The type check happens in step 4 only after passes were looked up; because nothing is consumed before step 5, the order of checks cannot cause partial consumption.
- **Pass selection**: the first unused pass in storage order (deterministic).
- **Parsing**: `ATTRACTION ENTER` with 3 parameters requires the literal `SPEED`.

## Risks / Trade-offs

- [Two state changes (occupancy in memory, consumption on file)] → consumption is persisted before reporting success; on failure the in-memory entry is rolled back, so both stay consistent.
