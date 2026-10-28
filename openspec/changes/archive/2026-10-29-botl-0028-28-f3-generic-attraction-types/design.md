# Design

## Context

`Attraction` is a concrete class; `AttractionService.enter(attractionId, Ticket, today)` implements entry for any ticket. Passes exist (`PassService`). See proposal.md.

## Goals / Non-Goals

**Goals:** capability interfaces + bounded generics with real effect; entry rules inside the attraction.
**Non-Goals:** SpeedPass entry and consumption (F3-C08).

## Decisions

- **Interfaces** (`business.model`): `AccessTicket` (`getId()`, `getDate()`, `individualTickets()`), `GeneralAccess`, `IndividualAccess`, `GroupAccess` extending it. `IndividualTicket implements IndividualAccess, GeneralAccess`; `GroupTicket implements GroupAccess, GeneralAccess`.
- **`abstract class Attraction<T extends AccessTicket>`** with `Class<T> accepted`, `getType()`, and `final List<User> enter(T ticket, LocalDate today, PassLookup passes)`: date check, `checkAccess(ticket, passes)` hook, duplicates, capacity, then all users enter. `accepts(AccessTicket)` / `enterAny(AccessTicket, ...)` bridge for callers that only hold the common type (cast via `accepted`).
- **Subclasses**: `GeneralAttraction extends Attraction<GeneralAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding `checkAccess` to require an unconsumed `SpecialPass` from `PassLookup`.
- **`AttractionType`** enum with `create(id, name, capacity)` factory and `fromName`.
- **`PassLookup`** (functional interface in model): `List<Pass> passesOf(IndividualTicket)`; `PassService` implements it and is injected into `AttractionService` by `ParkInitializer`.
- **Storage**: 4th column type; absent → GENERAL.

## Risks / Trade-offs

- [Raw `Attraction` references in repositories] → use `Attraction<?>` throughout.
