# Design

## Context

`CommandProcessor` is created once per execution by `App.run` and receives every command from both input modes; it is therefore the application's main controller. There are no date-dependent rules yet.

## Goals / Non-Goals

**Goals:** one date per execution, owned by the controller, settable by command, injectable for tests.

**Non-Goals:** Singleton, global static state, persistence of the date, a command to print the date.

## Decisions

- **Ownership:** a private `LocalDate currentDate` in `CommandProcessor`, exposed through `getCurrentDate()`. Future services that need the date will receive it from the controller (passed as an argument), so there is a single source. Alternative — a static/global clock — rejected: it would act as a de-facto Singleton, which the source explicitly does not require, and hampers test isolation.
- **Initialisation:** the existing constructor delegates to a new one taking the initial date, passing `LocalDate.now()`; tests use the explicit-date constructor. `LocalDate.now()` is called only there.
- **Parsing:** `LocalDate.parse(value, DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT))`, plus a length check of 10 characters so that only `yyyy-MM-dd` is accepted; `DateTimeParseException` → `ERROR:` with the date unchanged (assignment happens only after successful parsing).
- **Dispatch:** the operation (first `;` token, trimmed) is matched exactly against `HELP`, `EXIT`, `SYSTEM DATE`, `USER CREATE`, `USER DELETE`, `USER UPDATE`; everything else remains "Unknown command" as before.

## Risks / Trade-offs

- [Tests depending on the real date] → tests that inspect the startup date compare against `LocalDate.now()` taken around construction.
