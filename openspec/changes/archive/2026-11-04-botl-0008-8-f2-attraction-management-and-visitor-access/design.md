# Design

## Context

Tickets have a common abstract base `Ticket` (`IndividualTicket` with a user, `GroupTicket` composed of individual tickets); `TicketManager` holds standalone individual tickets and group tickets; `CommandProcessor` owns the system date and coordinates cross-entity rules. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One entry and one exit algorithm for both ticket kinds via polymorphism.
- Atomic, capacity-safe entry; live status.

**Non-Goals:**
- Attraction types, passes, reservations, attraction deletion/update commands, limiting a user to one attraction at a time, per-ticket usage counters (none in the source).

## Decisions

- **Polymorphic holders**: `Ticket` declares `abstract List<User> getHolders()`; `IndividualTicket` returns its user; `GroupTicket` returns the users of its constituent tickets. `Attraction.enter(Ticket, today)` and `Attraction.exit(Ticket)` only use `getDate()` and `getHolders()` — no `instanceof`.
- **`Attraction`**: `id`, `name`, `capacity`, `LinkedHashSet`-like ordered list of present `User`s (membership by identity). `enter` validates date equality, already-present users and `holders.size() <= available` before adding anything, so it is all-or-nothing. `exit` removes holders that are present; error if none.
- **Ticket lookup for access**: `TicketManager.findForAccess(id)` returns a standalone individual ticket or a group ticket (constituent ids are not accepted on their own, so a group is admitted as a unit).
- **`AttractionManager`** owns attractions (persisted definitions via `AttractionFileStore`, `Id;Nombre;Capacidad`, save-then-swap on create) and exposes `create`, `enter`, `exit`, `status`, `removeUserEverywhere`. Occupancy is in memory only, so each start is empty.
- **Status format** (multi-line, one `Result`):
  ```
  OK: Attraction A01 - Tornado
  Capacity: 20
  Present: 3
  Available: 17
  Users present: Ana Garcia Lopez (ana@correo.com), ...
  ```
- **Capacity validation** without regex: nonempty, ASCII digits only, parsed as `int` (guarded against overflow), must be > 0.
- **User deletion**: after a successful `USER DELETE`, the controller also removes the user from every attraction so status never lists a deleted user.

## Risks / Trade-offs

- [Same user present in several attractions at once] → not restricted by the source; left allowed.
- [Re-entry with the same ticket after exiting] → allowed while the ticket is valid today; the source does not limit uses.

## Open Questions

- **Exit command contract**: this source specifies `ATTRACTION EXIT;A01;T001` (ticket identifier); the brief reports an earlier Delivery 2 document using an email address. Implemented: ticket identifier, as in the authoritative source. Supporting the email form would add a second contract and needs a product decision; it does not change the attraction model.
