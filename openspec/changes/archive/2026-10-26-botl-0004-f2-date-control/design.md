# Design

## Context

`App.main` wires `UserRepository` → `UserManager` → `CommandProcessor`. There is no object representing the application as a whole yet, and no time-dependent rule exists so far; ticket and attraction rules in later changes will need the date.

## Goals / Non-Goals

**Goals:**
- One place that owns the current date, passed explicitly to whoever needs it.
- Deterministic tests: the startup date comes from an injectable `java.time.Clock`.

**Non-Goals:**
- Singleton (explicitly not required in this delivery), persisting the date, time of day.

## Decisions

- **`MainController`** (new class) holds `LocalDate currentDate`, created with a `Clock` (`Clock.systemDefaultZone()` in `main`); `getCurrentDate()` / `setCurrentDate(LocalDate)`. `LocalDate.now(clock)` in its constructor is the only place the real date is read. Later managers receive the same `MainController` instance to read the date. Alternative: a static field — rejected, it hides the dependency and complicates tests.
- **Parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`: rejects `2026-9-15`, `15/09/2026` and `2026-02-30`. The parse helper lives in `MainController.parseDate(String)` so later commands with dates (tickets) reuse it; it throws `UserException`-style validation errors. To keep error handling uniform the existing `UserException` is generalised: a new `ValidationException` would duplicate it, so `UserException` is renamed to `ParkException` (one unchecked exception for rejected operations, message shown to the user).
- **`CommandProcessor`** receives the `MainController`; `SYSTEM DATE;yyyy-MM-dd` is added to `COMMAND_SYNTAX` (so HELP lists it) and responds `OK SYSTEM DATE SET -> <date>`.

## Risks / Trade-offs

- [Renaming `UserException` touches several files] → Mechanical rename covered by the existing tests; it avoids a second exception type with identical meaning.
