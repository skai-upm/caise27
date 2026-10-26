# Design

## Context

`Attraction<T extends AttractionAccess>` has `admit(AttractionAccess)` (runtime kind check) and typed `enter(T)` (atomic: checks presence and capacity, then adds users). `SpecialAttraction` binds `T` to `IndividualAccess`. Passes live in `IndividualTicket` (`getSpeedPasses()`, `getSpecialPass()`), `Pass.consume()`; `PassController.save()` persists them.

## Goals / Non-Goals

**Goals:** SPEED and Special Pass rules per attraction type; all checks before any change; consumption reported and persisted.

**Non-Goals:** pass refunds or deletion cascades (covered elsewhere).

## Decisions

- **Individual tickets of an access**: `AttractionAccess.getIndividualTickets()` — an individual ticket returns itself, a group ticket its composition. Pass rules then work uniformly per member.
- **Template entry** in `Attraction`: `enter(T access, boolean speed)` = (1) `passesToConsume(access, speed)` — validation only, returns the passes to consume; (2) presence/capacity validation; (3) mutation: add users and `consume()` the selected passes; returns them. `enter(T)` = `enter(access, false)`; `admit(access, speed)` bridges with the kind check.
- **Default pass step** (`Attraction.passesToConsume`): no SPEED → none; SPEED → for each individual ticket the first unused SpeedPass, failing on the first member without one. This covers GENERAL/INDIVIDUAL individual entries and GENERAL/GROUP group entries, because kind restrictions already exclude other combinations.
- **`SpecialAttraction` override**: SPEED → reject; otherwise the ticket's Special Pass must exist and be unused, returned for consumption.
- **Controller**: `AttractionController.enter(attractionId, ticketId, speed)` keeps the date check, calls `admit`, and if passes were consumed calls the injected `PassController.save()`. `configure(AttractionRepository, TicketController, PassController, SystemDate)`; `App.initialize` configures `PassController` before `AttractionController`.
- **Command**: 2 or 3 parameters; the third must be `SPEED` (case-insensitive). Response appends `[SpeedPass consumed: …]` / `[Special Pass consumed: …]`.

## Risks / Trade-offs

- [Choosing which unused SpeedPass to consume] → the first unused in purchase order; the response names it.
