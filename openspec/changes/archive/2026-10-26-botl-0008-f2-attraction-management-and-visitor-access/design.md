# Design

## Context

`Ticket` is abstract with `IndividualTicket` and `GroupTicket`, both exposing `getUsers()`; `TicketManager.findById` finds either kind. `SystemDate` provides the current date.

## Goals / Non-Goals

**Goals:** attraction model; uniform, atomic ticket-based access; status output.

**Non-Goals:** attraction types and passes (Delivery 3), persisting occupancy.

## Decisions

- **`Attraction`**: `id` (final), `name`, `capacity`, `List<User> present`. `enter(Ticket)` takes `ticket.getUsers()`, checks none is already present and that `present.size() + users.size() <= capacity`, then adds all — the check-then-add happens before any mutation, so entry is atomic. `exit(Ticket)` removes the ticket's users that are present. Polymorphism through `Ticket.getUsers()` is the uniform mechanism; the attraction never inspects the concrete ticket class. Alternative: `instanceof` branches in the attraction — rejected by the spec.
- **`AttractionManager`** (receives `AttractionRepository`, `TicketManager`, `SystemDate`): create/delete/enter/exit/status. It resolves tickets via `TicketManager.findById` and checks `ticket.getDate().equals(systemDate.get())` on entry only.
- **Persistence**: `data/attractions.txt`, `Id;Nombre;Capacidad`; occupancy is never stored.
- **Status format** (multi-line):
  ```
  Attraction A01 - Tornado
    Capacity: 20
    Present: 2
    Available: 18
    Users present: Pedro Lopez Martin (87654321X, pedro@correo.com), ...
  ```
- **`ATTRACTION DELETE`** is added so attractions can be fully managed; it requires the attraction to be empty.

## Risks / Trade-offs

- [Same user holding two valid tickets could try to enter twice] → rejected as "already present".
