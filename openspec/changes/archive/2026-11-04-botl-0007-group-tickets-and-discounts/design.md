# Design

## Context

`Ticket` (individual) holds id, date, price, an owner reference and the owner DNI. `TicketManager` keeps an id-ordered map, saves to `tickets.txt` and answers `hasTicketOnOrAfter(user, today)`. `GroupManager` has no knowledge of tickets. `CommandProcessor` owns the date and price and runs cross-component checks (e.g. the user-deletion rule). See proposal.md.

## Goals / Non-Goals

**Goals:**
- A common ticket abstraction with inheritance; group ticket = aggregation of the group + composition of individual tickets.
- Existing file formats and command replies stay unchanged.

**Non-Goals:**
- Attraction access, re-pricing existing tickets, partial deletion of group tickets.

## Decisions

- **Hierarchy:**
  - abstract `Ticket` holds `id` and `date`, with abstract `getPrice()`.
  - `IndividualTicket` is the renamed current class, with owner, owner DNI and price.
  - `GroupTicket` holds a `Group group` reference (`null` for history after the group is deleted), a `groupId`, the `individualPrice`, the `total`, and an unmodifiable `List<IndividualTicket> constituents` created and owned by the group ticket.
  - `getPrice()` returns the individual price or the group total respectively.
  - Alternative of an interface rejected: both share state (id, date), so an abstract class avoids duplication.
- **Discount:** a pure static `GroupTicket.discountPercent(int members)` with `members < 3 → 0`, `≤ 5 → 10`, `≤ 10 → 20`, else `50`. Total is `price × n × (100 − d) / 100`, rounded `HALF_UP` to 2 decimals.
- **Constituent ids:** `<id>-<k>` (k = 1..n). They are deterministic, so the file only stores owner DNIs. At creation, the group id and every derived id must be unused among standalone and constituent ids (one id space).
- **`TicketManager`** keeps two maps, standalone individual tickets and group tickets, saved through two storages:
  - `hasTicketOnOrAfter(user, today)` checks standalone tickets and every constituent.
  - New `hasGroupTicketOnOrAfter(group, today)`, `createGroupTicket(...)` and `deleteGroupTicket(id, today)`.
  - The constructor receives `UserManager` and `GroupManager` to resolve DNIs and group ids.
  - `TICKET DELETE` only looks at standalone tickets, so constituents cannot be deleted individually.
- **Group lock in the controller:** before `GROUP ADD/REMOVE/DELETE`, `CommandProcessor` resolves the group and rejects the command when `tickets.hasGroupTicketOnOrAfter(group, currentDate)` holds. This keeps `GroupManager` independent of tickets, mirroring the user-deletion rule.
- **Empty group:** rejected. A group ticket with no constituents is meaningless, and the source defines the ticket as composed of its members' tickets.
- **Date rule:** the group ticket date must be ≥ system date. The source does not state this explicitly; it follows individual tickets, and constituents are individual tickets.
- **Persistence:** `group-tickets.txt`, format `id;groupId;date;price;total;dni...`; loaded after groups. Unknown group ids or DNIs keep the stored identifiers (history).
- **Literals:** `TG01` and `TG001` in the source are just distinct example ids; no renaming rule.

## Risks / Trade-offs

- [Constituent ids could collide with a later `TICKET CREATE;TG01-1;...`] → Prevented by the single id space check.
- [Renaming `Ticket` to `IndividualTicket` touches several files] → A mechanical rename, covered by the existing tests.
