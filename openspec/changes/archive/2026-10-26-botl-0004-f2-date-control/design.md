# Design

## Context

`CommandProcessor` dispatches every command and is the application's main controller; `App` wires it with `UserManager`. No component uses dates yet. The Singleton pattern is explicitly not required in this delivery.

## Goals / Non-Goals

**Goals:**
- One date object, owned by the main controller and shared by reference with future time-dependent managers.

**Non-Goals:**
- Persisting the date, Singleton, time-of-day handling.

## Decisions

- **`SystemDate` holder class** with `get()`/`set(LocalDate)`, initialised from `LocalDate.now()` (the only place reading the real clock). `CommandProcessor` receives it in its constructor and exposes `getSystemDate()`; future managers receive the same instance from `App`. Alternative: a plain `LocalDate` field in `CommandProcessor` — rejected because managers would need a back-reference to the processor to read it; alternative: static global — rejected (hidden global state, harder to test).
- **Strict parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` so `2026-9-15` and `2026-02-30` fail.
- **Response**: `OK SYSTEM DATE -> 2026-09-15`.

## Risks / Trade-offs

- [Code may later call `LocalDate.now()` directly] → specs require using `SystemDate`; reviews/tests check business rules against a set date.
