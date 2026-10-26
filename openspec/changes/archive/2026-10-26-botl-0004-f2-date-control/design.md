# Design

## Context

`App` wires `UserRepository → UserManager → CommandProcessor → CommandRunner`. `CommandProcessor` is the application's main controller (it dispatches every command). See proposal.md for motivation. The delivery explicitly does not require the Singleton pattern.

## Goals / Non-Goals

**Goals:**
- One mutable date object created once at startup and shared by reference with every component that needs it.

**Non-Goals:**
- Singleton or a global static; persistence of the date.

## Decisions

- **`SystemDate` class** wrapping a `LocalDate` with `get()`/`set()`. `App` creates one instance (`SystemDate.today()` uses `LocalDate.now()` — the only place the real clock is read) and passes it to `CommandProcessor`; future managers needing the date receive the same instance. Alternative: a `LocalDate` field in `CommandProcessor` passed by value to each operation — rejected because managers created later would need the date threaded through every call and could cache stale values.
- **Parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` so `2026-02-30` is rejected (not resolved to 2026-02-28). `SystemDate.parse` throws `IllegalArgumentException`, which `CommandProcessor` turns into `ERROR Invalid date ...`.
- **Command output**: `OK SYSTEM DATE yyyy-MM-dd`.

## Risks / Trade-offs

- [Some future code could call `LocalDate.now()` directly] → convention plus review; tests set explicit dates.
