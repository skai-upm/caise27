# Design

## Context

`CommandProcessor` is the application's main controller (one instance per execution, shared by interactive and file modes). See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One date value per execution, owned by the controller and readable by future time-dependent operations.

**Non-Goals:**
- Singleton, global static state or a clock abstraction (explicitly not required); persisting the date.

## Decisions

- **`LocalDate currentDate` field in `CommandProcessor`**, initialised with `LocalDate.now()` in the constructor — the only place the real clock is read. Exposed through `getCurrentDate()`; future operations receive the date as a parameter from the controller. An overloaded constructor taking the initial date keeps tests deterministic. Alternative: static holder class — rejected, it would be a hidden global and the proposal asks for the date in the controller.
- **Parsing with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`**: rejects non-padded fields and impossible dates such as 2026-02-30. `DateTimeParseException` is translated into a user-facing `Invalid date` error.
- **Command routing**: the existing two-word operation dispatch gets a `SYSTEM` domain with action `DATE`.

## Risks / Trade-offs

- [Future code calling `LocalDate.now()` directly] → spec requirement plus a test asserting the controller date is what operations see; reviewers check for `now()` usage.
