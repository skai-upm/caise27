# Design

## Context

Tickets form a hierarchy (`Ticket` ← `IndividualTicket`, `GroupTicket` composed of `IndividualTicket`s). The controller (`CommandProcessor`) owns the system date and coordinates managers. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One entry/exit algorithm for every ticket type through polymorphism.

**Non-Goals:**
- Speed passes / special passes, attraction types, per-ticket usage limits (later deliveries).

## Decisions

- **Interface `Admission`** with `List<User> getAdmittedUsers()` and `boolean isValidOn(LocalDate)`; `Ticket` implements it (`isValidOn` = date equals the given day). `IndividualTicket` returns its owner (empty if detached); `GroupTicket` returns the owners of its components. `Attraction` only depends on `Admission`, so later pass types can plug in. Alternative: `instanceof` branching in `Attraction` — rejected (duplicated access logic, explicit requirement for polymorphism).
- **`Attraction`**: `id`, `name`, `capacity`, `List<User> present`. `enter(Admission, LocalDate)` validates date, non-empty users, not already present, capacity (`present + n <= capacity`) before mutating → atomic. `exit(Admission)` removes the present ones. `getAvailable()` = capacity − present.
- **`AttractionManager`**: map + `AttractionStorage` (`data/attractions.txt`, `Id;Nombre;Capacidad`); occupancy is in-memory only. `removeUserEverywhere(User)` used by USER DELETE.
- **Ticket lookup for entry**: `TicketManager.findAdmission(id)` returns a top-level ticket or an internal ticket (so members can also enter alone with their own internal ticket).
- **STATUS output** (multi-line):
  ```
  Attraction A01 - Tornado
    Capacity: 20 | Present: 3 | Available: 17
    Users present: Ana Garcia Lopez <ana@correo.com>, ...
  ```
  (`(none)` when empty).

## Risks / Trade-offs

- [Date changes while users remain inside] → occupancy persists in memory until EXIT; consistent with "date checked on entry only".
- [Exit with a group ticket when some members already left] → removes those still present; error only if none is present.
