# Design

## Context

`Ticket` (individual, owner reference + owner DNI) is managed by `TicketService`, which already answers `hasActiveTickets(user, today)` for USER DELETE. `CommandProcessor` holds the system date and price and does cross-service checks (e.g. USER DELETE). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** a common ticket abstraction, a composed group ticket with frozen membership, exact discount brackets, group locks and cascading deletion.

**Non-Goals:** attraction admission; changing individual ticket behaviour or file format.

## Decisions

- **`ParkTicket`** (abstract): `id`, `date`, abstract `getPrice()`. `Ticket` extends it unchanged in behaviour. `GroupTicket` extends it with `group` (aggregation: reference to the `Group`, plus `groupId` kept for history), `unitPrice` and `tickets` (composition: the constituent `Ticket`s are created by the `GroupTicket` constructor from the member snapshot and never shared). `getPrice()` returns the discounted total. Alternative — an interface — rejected because both kinds share id/date state.
- **Constituent tickets:** ids `<groupTicketId>-<n>` (n = 1..members), date of the group ticket, price = individual price used for the purchase. They are not in the individual ticket list, so `TICKET DELETE` cannot remove them.
- **Discount** in `GroupTicket.discountFor(int members)`: `<3 → 0`, `≤5 → 10`, `≤10 → 20`, else `50` percent; total = `unitPrice × n × (100 − d) / 100`, scale 2 `HALF_UP`.
- **`TicketService`** now also holds group tickets: `createGroupTicket(id, group, date, today, price)`, `deleteGroupTicket(id, today)` (only `date > today`, removes the whole composition), `isGroupLocked(group, today)` (any group ticket of that group with `date >= today`). Ticket ids must be unique across individual tickets, group tickets and constituent tickets. `hasActiveTickets(user, today)` iterates over all individual tickets including constituent ones through the common abstraction.
- **Empty group:** purchase rejected (assumption: the source only describes groups with members; a zero-ticket composition would be meaningless).
- **Lock check in the controller:** before `GROUP ADD/REMOVE/DELETE`, `CommandProcessor` rejects the command if `tickets.isGroupLocked`. This keeps `GroupService` free of a dependency on tickets (no cycle). `TicketService` takes `GroupService` only to restore group tickets.
- **`GroupTicketFileStorage`**: `grouptickets.txt`, `Id;IdGrupo;yyyy-MM-dd;PrecioTotal;PrecioIndividual;DNI,...`. On load, group by id and users by DNI; missing group or user kept as historical identifiers (same approach as individual tickets). The stored total is used as-is.
- **Literals:** `TG01` and `TG001` from the source are used as distinct examples in tests.

## Risks / Trade-offs

- [Group deleted after its tickets became past] → historical group ticket keeps `groupId` only after restart; it can no longer lock anything, which matches "historical".
- [Constituent ids derived from the group ticket id] → an individual `TICKET CREATE;TG01-1;...` is rejected as a duplicate id, keeping ids unique.
