# Design

## Context

`Attraction` is a concrete class whose `enter(Ticket)` uses `Ticket.getUsers()` polymorphically (atomic, capacity-checked). `AttractionController.enter` resolves the ticket and checks its date. Tickets: abstract `Ticket` → `IndividualTicket`, `GroupTicket`. Attractions persist as `Id;Nombre;Capacidad`.

## Goals / Non-Goals

**Goals:** types that restrict ticket kinds through interfaces + bounded generics used functionally; backward-compatible syntax and data.

**Non-Goals:** Special Pass / SpeedPass consumption rules (next change).

## Decisions

- **Access capabilities** (`business.model`):
  - `AttractionAccess` — `getId()`, `getDate()`, `getUsers()`; abstract `Ticket` implements it.
  - `IndividualAccess extends AttractionAccess` — `getUser()`; implemented by `IndividualTicket`.
  - `GroupAccess extends AttractionAccess` — `getTickets()` (the individual tickets); implemented by `GroupTicket`.
- **Generic attraction**: `abstract class Attraction<T extends AttractionAccess>` keeps id, name, capacity and present users, plus `Class<T> acceptedType`. `enter(T access)` is the typed, atomic entry; `exit(AttractionAccess)` accepts any ticket (leaving needs no restriction). `admit(AttractionAccess access)` is the runtime bridge used by the controller: if `acceptedType.isInstance(access)` it calls `enter(acceptedType.cast(access))`, otherwise it rejects with the type's name. The generic parameter therefore determines both the compile-time signature and the runtime check — it is not decorative.
- **Subclasses**: `GeneralAttraction extends Attraction<AttractionAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (separate class so the next change adds Special Pass rules there). Each returns its `AttractionType`.
- **`AttractionType` enum** (`GENERAL, INDIVIDUAL, GROUP, SPECIAL`) with `create(id, name, capacity)` acting as factory for the matching subclass.
- **Controller**: stores `List<Attraction<?>>`; `create(id, name, capacity, type)`; `enter` keeps the date check and calls `admit`.
- **Command**: 3 parameters → `GENERAL`; 4 parameters → parse `Tipo` (case-insensitive).
- **Persistence**: `Id;Nombre;Capacidad;Tipo`; 3-field lines load as `GENERAL`.
- **Status** adds a `Type:` line.

## Risks / Trade-offs

- [Wildcard `Attraction<?>` in the controller] → the controller only uses `admit`/`exit`/getters, which need no type argument; typed entry stays available to code that knows the concrete type.
