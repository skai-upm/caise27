# Design

## Context

`Ticket` (abstract) / `IndividualTicket` and a `TicketRepository` with one identifier space exist; groups have no lock and cannot be deleted yet.

## Goals / Non-Goals

**Goals:** group tickets reuse the ticket abstraction and individual-ticket pricing; locked state is computed. **Non-Goals:** persistence, attraction access.

## Decisions

- **`GroupTicket extends Ticket`**: holds the immutable composition `List<IndividualTicket>`, a nullable `Group` reference (`detachGroup()` on group deletion) and the group identifier for history. `getPrice()` sums the composition's prices — price behavior reused rather than duplicated. No user list: members are reached through the composition tickets.
- **`GroupDiscount`** (enum of brackets with `forMembers(n)`) applies the percentage to the base price per individual ticket, rounded to cents (HALF_UP).
- **Atomic creation**: build every identifier (`TG`, `TG:dni`…) and call `TicketRepository.ensureAvailable` once, then store; nothing is stored if the check fails.
- **Derived queries in `TicketRepository`**: `groupTicketsOf(Group)`, `isLocked(Group, today)`, `containerOf(IndividualTicket)` — all by traversal, so groups/users never hold ticket references.
- **Services** receive the extra repositories they need (`GroupService` gets tickets for locks and deletion; `TicketService` gets groups); constructors without them keep fresh empty repositories for older tests.
- Responses: `GROUP TICKET CREATED. Total price: 192.00 EUR`, `GROUP TICKET DELETED`, `GROUP DELETED`.

## Risks / Trade-offs

- [Traversal is O(tickets)] → acceptable for course-sized data.
