# Design

## Context

`Ticket` is today a concrete individual ticket (`id`, `date`, `price`, `user`) managed by `TicketManager` and persisted by `TicketFileStore` in `tickets.txt` (`Id;date;price;DNI`, unresolved lines kept as history). The controller (`CommandProcessor`) owns system date and price and already enforces the ticket rule on `USER DELETE`. `GroupManager` handles membership without knowledge of tickets or dates. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Inheritance-based common ticket abstraction; group ticket composed of individual tickets; group aggregated by reference.
- Locks evaluated against the configured system date at command time.
- No change to `tickets.txt` format (persisted-data compatibility).

**Non-Goals:**
- Attraction admission, partial refunds, editing a group ticket.

## Decisions

- **Hierarchy**: `abstract class Ticket` (`id`, `date`, `price`, getters); `IndividualTicket extends Ticket` (adds `user`) — the existing class renamed; `GroupTicket extends Ticket` (adds `group` reference, `basePrice`, unmodifiable `List<IndividualTicket> tickets`). Group ticket `price` is the discounted total.
- **Constituent identifiers**: `<groupTicketId>-<memberDni>`; unique within the group ticket because members are unique. Creation is rejected if any constituent id collides with an existing id.
- **Discount**: static `GroupTicket.discountPercent(int members)` (0/10/20/50 by the brackets); total = `basePrice × n × (100 − d) / 100`, scale 2, `HALF_UP` (exact for two-decimal prices with these percentages).
- **`TicketManager`** now holds `individualTickets`, `groupTickets`, and `reservedIds` from historical lines of both files. New operations: `createGroupTicket(id, group, date, today, price)`, `deleteGroupTicket(id, today)`, `isGroupLocked(group, today)`, `allTickets()` (common abstraction). `hasTicketOnOrAfter(user, today)` also scans constituent tickets. `delete(id, today)` rejects ids of constituent tickets. Both files are saved on group-ticket changes only for `grouptickets.txt` (individual file untouched); save-then-swap atomicity as before.
- **`GroupTicketFileStore`**: line `Id;date;total;groupId;basePrice;DNI...`; `load(groupById, userByDni)` restores linked group tickets; lines with unknown group/DNI or malformed fields become historical lines (preserved verbatim); reserved ids include the line id and every `<id>-<dni>`.
- **Locks in the controller**: before delegating `GROUP ADD|REMOVE|DELETE`, `CommandProcessor` looks up the group and rejects if `tickets.isGroupLocked(group, currentDate)`. `GroupManager` stays independent of tickets and dates.
- **Wiring**: `TicketManager(TicketFileStore, GroupTicketFileStore, UserManager, GroupManager)`; `App` adds `grouptickets.txt` in the data directory.
- **Output**: `Result.verbatim(true, "OK GROUP TICKET CREATED -> " + total)`.

## Risks / Trade-offs

- [Group deleted after its tickets became past] → the group ticket keeps its in-memory reference this session; after restart it becomes a historical line. History is preserved; identifiers stay reserved.
- [Empty group purchase] → rejected: the source composes the ticket from members, and a zero-ticket purchase has no meaning.
- [Deleting a constituent ticket alone would break the composition] → `TICKET DELETE` rejects constituents; `GROUPTICKET DELETE` removes them together.
