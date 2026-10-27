# Design

## Context

Delivery 1 wiring: `App` builds `UserRepository` (file storage) → `UserService` → `CommandProcessor`. Delivery 2 requires one central controller for the whole park (users now; groups, tickets, attractions later) with a replaceable date and price. Delivery 1 restrictions (standard library only, no regular expressions) still apply.

## Goals / Non-Goals

**Goals:**
- Guaranteed single controller instance; per-execution date and price; SYSTEM commands.

**Non-Goals:**
- Tickets/groups/attractions (F2-C02..F2-C07); persisting date or price (the price explicitly resets each execution; the date is the start date each execution).

## Decisions

- **Singleton `ParkController`:** private constructor, `private static final ParkController INSTANCE`, `getInstance()`. Eager initialisation is thread-safe and guarantees one instance wherever it is requested. F3-C03 (singleton + dependency injection) may refine this later.
- **Per-execution state via `initialize(UserRepository users, LocalDate today)`:** called once by `App.run` at startup; sets users, date = today, price = 40.00. Since the instance lives as long as the JVM, re-initialising at each `App.run` keeps tests (several runs in one JVM) consistent with "each execution starts at 40.00".
- **Date parsing:** `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`, so impossible dates (`2027-02-30`) and other layouts are rejected.
- **Price:** `BigDecimal` with scale 2. Accepted text (ambiguity, flagged): digits with an optional `.` and at most two decimals, value > 0 (`45.50`, `45.5`, `45`). Signs, exponents, commas and more than two decimals are rejected because they are not plain euro amounts. Checked character by character (no regex).
- **Output:** `SYSTEM DATE UPDATED: 2027-04-15` and `SYSTEM PRICE UPDATED: 45.50`; errors use `ERROR:`.
- **Mandatory commit:** `[F2-C01] Add park controller and system date` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Global mutable singleton complicates tests] → Tests call `initialize` in setup; `App.run` always re-initialises.
