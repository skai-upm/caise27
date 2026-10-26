# Design

## Context

`Attraction` (id, name, capacity, present users) admits any `Admission`; `Ticket implements Admission`; passes exist on `IndividualTicket` (botl-0016). `AttractionStorage` lines `Id;Nombre;Capacidad`. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Ticket-kind restrictions expressed by type parameters and capability interfaces; SPECIAL logic written against the typed ticket.

**Non-Goals:**
- SpeedPass usage at attractions (later deliveries), changing attraction types after creation.

## Decisions

- **Capability interfaces** (model): `IndividualAccess extends Admission` exposes `getSpecialPass()`; `GroupAccess extends Admission` (marker of group admission). `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess` (through `Ticket implements Admission`).
- **Bounded generic class** `Attraction<T extends Admission>` with `Class<T> acceptedType` (needed at runtime because tickets are looked up by id as `Ticket`). `enter(Admission, LocalDate)` checks `acceptedType.isInstance` → `cast` → `enterAdmitted(T, LocalDate)`, which runs the common checks plus hooks `checkAccess(T)` (before any change) and `onEntry(T)` (after users are added). Atomicity is preserved because hooks validate before mutation.
- **`SpecialAttraction extends Attraction<IndividualAccess>`**: `checkAccess` requires `admission.getSpecialPass()` AVAILABLE; `onEntry` consumes it — no casts, the bound gives access to the typed API (functional use of generics).
- **`AttractionType`** enum `GENERAL, INDIVIDUAL, GROUP, SPECIAL`, each constant implementing `create(id, name, capacity)` returning `Attraction<Admission>`, `Attraction<IndividualAccess>`, `Attraction<GroupAccess>` or `SpecialAttraction`. Used by the manager and by the storage.
- **Manager/controller**: `AttractionManager` keeps `Map<String, Attraction<?>>`; `create(id, name, capacityText, AttractionType)`. `ParkController.createAttraction(id, name, capacity, typeText)` parses the type (`null` → GENERAL).
- **Persistence**: lines `Id;Nombre;Capacidad;Tipo`; 3-field legacy lines load as GENERAL; `ParkController`'s ticket save path untouched.
- **Presentation**: `ATTRACTION CREATE` accepts 3 or 4 parameters; catalog gets `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`; STATUS header `Attraction A01 - Tornado (GENERAL)`.

## Risks / Trade-offs

- [Unchecked cast warnings in `AttractionType`] → none: each constant constructs with a concrete `Class<T>` literal.
