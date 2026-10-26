# Design

## Context

botl-0017: `Attraction<T extends Admission>` with hooks `checkAccess(T)`/`onEntry(T)`; `SpecialAttraction` consumes the Special Pass in `onEntry` but nothing persists it. Passes live in `IndividualTicket`; `TicketManager` persists tickets+passes. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One validate-then-commit algorithm for normal, SPEED and SPECIAL entries; consumption reported and saved.

**Non-Goals:**
- Using several SpeedPasses per person, SpeedPass refunds on exit.

## Decisions

- **Capabilities**: `Admission.getIndividualAccesses()` — an individual ticket returns itself, a group ticket its internal tickets (each an `IndividualAccess`). `IndividualAccess.findAvailableSpeedPass()` returns the first AVAILABLE SpeedPass or null.
- **`Attraction.enter(Admission, LocalDate, boolean speed)` → `EntryResult`** (record: users entered, consumed passes). Steps: type check → date, users, duplicates, capacity → `checkAccess(T)` → `passesToConsume(T)` (hook; Special returns its pass) → if `speed`: `allowsSpeed()` (false for SPECIAL) and one available SpeedPass per individual access, all collected before any change → add users → consume collected passes. Failing anywhere before the commit point changes nothing. `onEntry` hook is replaced by `passesToConsume` (consumption is generic).
- **Persistence**: `TicketManager.recordPassUsage()` saves tickets (with pass statuses); `ParkController.enterAttraction(attractionId, ticketId, speed)` calls it when passes were consumed.
- **Presentation**: optional 4th parameter must equal `SPEED` (case-insensitive); response `OK: 1 user(s) entered attraction A01; consumed SpeedPass T001-S1` (comma-separated list when several). Catalog adds `ATTRACTION ENTER;Id;IdTicket;SPEED`.

## Risks / Trade-offs

- [Same user twice inside a group with SPEED] → the duplicate check fails first; nothing consumed.
