# Design

## Context

Tickets share an abstract `Ticket` base with `IndividualTicket` (owner) and `GroupTicket` (constituents). `TicketManager` exposes standalone and group tickets, and `CommandProcessor` owns the system date. See proposal.md.

## Goals / Non-Goals

**Goals:**
- One entry/exit path in `Attraction` for every ticket kind, through polymorphism.
- Occupancy kept in memory only.

**Non-Goals:**
- Attraction types, passes, reservations, attraction deletion or update commands (not requested).

## Decisions

- **Polymorphic holders:** `abstract List<User> getHolders()` on `Ticket`. `IndividualTicket` returns its owner; `GroupTicket` returns the owners of its constituents (it delegates to each constituent's `getHolders()`). `Attraction.enter(Ticket, LocalDate today)` and `exit(Ticket)` never test the concrete type. Alternative of `instanceof` branches rejected: the source asks for uniform OO handling.
- **`Attraction`:** final `id`, `name`, `capacity` and a `LinkedHashSet<User> present` of the same user objects. A user listed once cannot be counted twice. Entry checks, in order:
  1. date equals today;
  2. holders nonempty and all resolvable (non-null);
  3. none already present;
  4. `present + holders ≤ capacity`.
  It then adds all holders, so failures leave the attraction untouched (atomic). Exit removes the holders that are present and fails if none was.
- **Already-present rule:** re-entering with the same ticket is rejected to avoid double counting. It is limited to the same attraction; the source says nothing about being in two attractions.
- **Ticket lookup:** `TicketManager.findAnyTicket(id)` returns a standalone individual or a group ticket. Constituent ids are not accepted on their own, consistent with `TICKET DELETE`.
- **Exit contract:** the source specifies `ATTRACTION EXIT;IdAtraccion;IdTicket` (example `ATTRACTION EXIT;A01;T001`), so the ticket identifier is used.
- **Persistence:** `AttractionFileStorage` (`attractions.txt`, `id;name;capacity`); `AttractionManager` loads definitions at startup, so occupancy starts empty.
- **Capacity parsing:** digits only (no regex), parsed with `Integer.parseInt`; overflow and 0 are rejected.
- **STATUS format** (multi-line, starts with `OK`):
  ```
  OK: attraction A01 - Tornado
    capacity: 20
    present: 3
    available: 17
    users: Ana Garcia Lopez <ana@correo.com>, ...
  ```
  `users: (none)` when empty.

## Risks / Trade-offs

- [Open question: an earlier Delivery 2 document reportedly used an email for `ATTRACTION EXIT`] → The authoritative source text uses the ticket identifier, which is implemented. Switching to email later would only change the command parsing.
- [Exit by group ticket also removes a member who entered with another ticket] → Holders are users and the attraction does not track which ticket admitted whom. Acceptable, and documented.
