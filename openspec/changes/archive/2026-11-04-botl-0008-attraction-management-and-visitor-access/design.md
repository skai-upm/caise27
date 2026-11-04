# Design

## Context

Tickets form a hierarchy: abstract `Ticket`, `IndividualTicket` (with an optional `User`) and `GroupTicket` (composed of `IndividualTicket` parts). `TicketManager` owns both kinds. `CommandProcessor` holds the system date and orchestrates cross-entity rules. Entities follow the entity / manager / file-repository split.

## Goals / Non-Goals

**Goals:**
- One entry procedure for every ticket kind, driven by polymorphism.
- All-or-nothing entry with capacity never exceeded.
- Status computed from the live collection.

**Non-Goals:**
- Attraction types, passes, reservations, deleting or editing attractions (none are requested).
- Persisting occupants.

## Decisions

- **Polymorphic visitors.** `Ticket` declares `abstract List<User> getVisitors()`. `IndividualTicket` returns its user if it still exists, and `GroupTicket` returns the visitors of its parts. `Attraction.enter(Ticket, LocalDate today)` uses only the `Ticket` abstraction (date via `getDate()`, people via `getVisitors()`), so it has no type checks. Rejected alternative: `instanceof` branches in the attraction or controller, which the source explicitly asks to avoid.
- **`Attraction`**: `id` (final), `name`, `capacity` (final int), `List<User> present` (identity semantics, insertion order).
  - `enter` validates everything before mutating: date equals today, at least one visitor, no visitor already present, and `present.size() + visitors.size() <= capacity`. Only then does it add all visitors.
  - `exit(Ticket)` removes the ticket's visitors that are present and fails if none are.
  - `getAvailable()` is computed as capacity minus present.
  - Failures throw `UserException` (the general rejection type).
- **`AttractionManager`**: `LinkedHashMap<String, Attraction>`, `create(id, name, capacityText)`, `find`, `removeUserEverywhere(User)`. It persists definitions after `create` with rollback. Occupancy is in-memory only, so `enter`/`exit` do not touch the file.
- **`AttractionFileRepository`**: `attractions.txt` (sibling of the users file, git-ignored), lines `Id;Nombre;Capacidad`. Malformed lines are skipped with a warning.
- **Capacity parsing** is done without regex: digits only, then `Integer.parseInt` inside a try (overflow → error), and the value must be > 0.
- **Ticket lookup.** `TicketManager.findAnyTicket(id)` returns `Optional<Ticket>` among individual tickets, group tickets and constituent tickets. A group member can therefore use their own generated ticket (`TG01-<DNI>`). That ticket is a real individual ticket and the source does not forbid it.
- **Exit by ticket identifier.** The authoritative source here uses `ATTRACTION EXIT;A01;T001`, so exit takes a ticket identifier. For a group ticket, every present member of that group ticket leaves.
- **Status output.**
  ```
  OK: Attraction A01 (Tornado) capacity=20 present=4 available=16
    - Ana Garcia Lopez <ana@correo.com>
    ...
  ```
- **User deletion** also calls `attractions.removeUserEverywhere(user)` (derived consistency rule, like groups). A deleted user can no longer be "present".
- **Wiring.** `App.run` creates `AttractionManager` from `attractions.txt`. The `CommandProcessor` constructor gains the attraction manager.

## Risks / Trade-offs

- [A user may be present in several attractions at once] → the source does not forbid it, so it is allowed (open question).
- [Status line format is free-form] → contains every required field and is covered by tests.

## Open Questions

- The brief notes that an earlier Delivery 2 document used an email for `ATTRACTION EXIT` while this source uses a ticket identifier. This change implements the ticket-identifier form from the authoritative text. Adding an email form later would be additive and would not change these specs' behaviour for ticket identifiers.
- Whether one user may be inside two attractions simultaneously is not specified. It is currently allowed.
