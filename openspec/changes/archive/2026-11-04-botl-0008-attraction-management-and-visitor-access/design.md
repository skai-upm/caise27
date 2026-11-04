# Design

## Context

Tickets: abstract `Ticket` (id, date, price) with `IndividualTicket` (owner `User`) and `GroupTicket` (constituent `IndividualTicket`s). `TicketService.findAny(id)` resolves both kinds. `CommandProcessor` owns the system date. Repositories write `;` files into the data directory.

## Goals / Non-Goals

**Goals:** one access path for all ticket kinds (polymorphism), atomic capacity check, live status.

**Non-Goals:** attraction types, passes, reservations, persisting occupancy, editing/deleting attractions.

## Decisions

- **Polymorphic visitors**: `Ticket` declares `abstract List<User> getVisitors()`. `IndividualTicket` returns its owner (empty if the owner is no longer registered); `GroupTicket` returns its constituents' owners. `Attraction` only ever sees `Ticket` — no `instanceof`, no per-type branches. Alternative: an `Admittable` interface — rejected as redundant with the existing abstract base.
- **`Attraction`** (`id`, `name`, `capacity`, `LinkedHashSet`-like ordered `List<User> present` checked by identity): `enter(Ticket, LocalDate today)` validates date equality, non-empty visitors, none already present, `visitors.size() <= available()`, then adds all — validation happens entirely before mutation, so group entry is atomic. `exit(Ticket)` removes the visitors that are present; rejects if none. `available() = capacity − present.size()`.
- **`AttractionService(AttractionRepository)`**: create/find; occupancy lives only in memory.
- **Controller**: `ATTRACTION ENTER/EXIT` resolve the attraction (`AttractionService.find`) and the ticket (`TicketService.findAny`) and call `attraction.enter(ticket, currentDate)` / `exit(ticket)`. `ATTRACTION STATUS` formats:
  ```
  OK: Attraction A01 - Tornado
    Maximum capacity: 20
    Users present: 4
    Available places: 16
    Present: Ana Garcia Lopez <ana@correo.com>, ...   (or "Present: none")
  ```
- **Persistence**: `attractions.txt` `Identificador;Nombre;Aforo`; malformed lines skipped with a warning.

## Risks / Trade-offs

- [Users inside several attractions at once] → not prohibited by the source; documented as an assumption rather than invented.
- [In-memory occupancy] → by design ("attractions are always empty when the system starts").
