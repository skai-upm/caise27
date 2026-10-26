# Design

## Context

`Attraction` (model) has `enter(Ticket, LocalDate)` using the polymorphic `Ticket.getHolders()`; `AttractionController` resolves tickets by id from `TicketController` (returns `Ticket`) and stores attractions in a map; `AttractionRepository` writes `id;name;capacity`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Capability interfaces implemented by the ticket classes; generics whose bound is enforced and used in signatures.

**Non-Goals:**
- SpeedPass/Special Pass consumption at entry (C08).

## Decisions

- **Capability interfaces** (`business.model`):
  - `Accessible`: `getId()`, `getDate()`, `getHolders()` — anything usable to access an attraction. `Ticket` implements it.
  - `IndividualAccess extends Accessible`: `getUser()` — implemented by `IndividualTicket`.
  - `GroupAccess extends Accessible`: `getTickets()` (the individual tickets composing it) — implemented by `GroupTicket`.
- **Generic attraction**: `abstract class Attraction<T extends Accessible>` keeps id, name, capacity, visitors and a `Class<T> admitted` token. Typed core `enter(T access, LocalDate today)`; `admit(Accessible)` returns `admitted.cast(...)` or rejects with "does not admit"; `enterWith(Accessible, today)` = `enter(admit(a), today)` is what the controller calls, since ids resolve to the general `Ticket` type at runtime. `abstract AttractionType getType()`.
  - `GeneralAttraction extends Attraction<Accessible>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>`.
  - The bound is functional: `enter` is only callable with a `T`, and `admit` uses the class token derived from the bound to filter tickets. Alternative: a type enum with `instanceof` switches — rejected (the epic requires generics/interfaces).
- **`AttractionType` enum** with `create(id, name, capacity)` factory returning the matching subclass; parsing from the command and the file.
- **Persistence**: `id;name;capacity;type`; 3-field lines load as `GENERAL`. (C09 lists type persistence; it is done here so types never get lost between restarts.)
- **Status** adds a `Type: <type>` line after `Name`.
- **UML**: interfaces, `Attraction<T extends Accessible>` with the four bound subclasses, realizations by ticket classes.

## Risks / Trade-offs

- [Unchecked casts] → avoided: `Class<T>.cast` performs a checked runtime cast.
