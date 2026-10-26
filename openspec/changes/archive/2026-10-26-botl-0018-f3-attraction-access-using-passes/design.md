# Design

## Context

`Attraction<T extends Admission>.admit(Admission)` checks the accepted capability, calls the `checkAccess(T)` hook, then applies presence and capacity checks before adding visitors. `IndividualTicket` owns its SpeedPasses and Special Pass; `Pass.consume()` marks a pass. `AttractionController.enter(attractionId, ticketId)` checks the ticket date. `TicketController.save()` persists tickets and passes.

## Goals / Non-Goals

**Goals:**
- All checks before any change: select the passes to consume while validating, and consume them only after the users have been admitted.
- Attraction-type rules stay in the attraction classes (polymorphism, no type switches in controllers).

**Non-Goals:**
- Using SpeedPasses to skip capacity limits (no queue model exists).

## Decisions

- **Capabilities**: `Admission.getIndividualTickets()` — the individual tickets granting the access (`IndividualTicket` returns itself, `GroupTicket` its composition); `IndividualAdmission.getSpecialPass()`. `IndividualTicket.nextUnusedSpeedPass()` returns the first unconsumed SpeedPass or `null`.
- **`Attraction.admit(Admission ticket, boolean speed)`** returns `EntryResult(int admitted, List<Pass> consumed)` (model record). Order: capability check → `if (speed && !allowsSpeed()) reject` → `List<Pass> passes = passesFor(typed, speed)` (validation only) → presence and capacity checks → add visitors → `passes.forEach(Pass::consume)`. `admit(ticket)` keeps meaning normal entry.
- **`passesFor(T, speed)`** in the base class: with `speed`, one unused SpeedPass per individual ticket (`getIndividualTickets()`), rejecting the whole entry if any is missing; otherwise none. `SpecialAttraction` overrides it: speed is disallowed (`allowsSpeed()` false) and it returns the ticket's Special Pass, rejecting a missing or consumed one. The former no-op `checkAccess` hook is replaced by this method.
- **Controller**: `AttractionController.enter(attractionId, ticketId, speed)` keeps the date check, calls `admit`, and calls `ticketController.save()` when passes were consumed.
- **Command**: second registered syntax `ATTRACTION ENTER;IdentificadorAtraccion;IdentificadorEntrada;SPEED`; the fourth value must be `SPEED` (case-insensitive). Output: `OK ENTERED A01 -> 2 users, consumed SpeedPass SP1, SP4` / `…, consumed Special Pass XP1`.
- **UML**: `EntryResult` record with `admitted : int` and association `consumed` to `Pass`; usage from `Attraction`.

## Risks / Trade-offs

- [Persisting consumption can fail after visitors were added] → `TicketController.save()` reports the error; the in-memory entry stands for the session (presence is never persisted anyway).
