# Proposal

## Why

Groups exist but cannot buy anything together. This change lets a group buy one group ticket for a date, which generates an individual ticket for every member at that moment, applies a member-count discount, and locks the group (and its members' deletion) while the group ticket is current or future.

## What Changes

- Introduce a common ticket abstraction: abstract `Ticket` (identifier, date, price) specialised by `IndividualTicket` (the existing individual ticket, renamed) and the new `GroupTicket`.
- `GroupTicket` aggregates the `Group` that bought it and is composed of the generated `IndividualTicket`s, which reference their users like ordinary individual tickets.
- New command `GROUPTICKET CREATE;TG01;G01;2026-10-15`: non-empty existing group, date not before the system date, unique identifier; one individual ticket per current member at the current individual price; total = sum × (1 − discount) with discount 0% (< 3 members), 10% (3–5), 20% (6–10), 50% (> 10); answers exactly `OK GROUP TICKET CREATED -> <Precio>`.
- Later membership changes never alter a purchased group ticket's composition.
- New command `GROUPTICKET DELETE;TG001`: only when the date is strictly after the system date; deletes the group ticket and all its constituent individual tickets.
- **Group lock**: while the group has a group ticket with `ticketDate >= SYSTEM DATE`, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` are rejected; the lock ends as the system date passes the ticket dates.
- Constituent tickets block deletion of their users exactly like individual tickets (`ticketDate >= SYSTEM DATE`), and cannot be deleted on their own with `TICKET DELETE`.
- New file `grouptickets.txt`; constituent tickets stay in `tickets.txt` with the existing format.
- `HELP` and `uml/model.puml` (inheritance, aggregation, composition, multiplicities) updated.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket data, purchase, discount brackets, deletion, group lock and the ticket abstraction shared with individual tickets.
- `group-ticket-file-persistence`: Group ticket file format and restoration of the group and constituent relationships.

### Modified Capabilities
- `user-groups`: "Add member", "Remove member" and "Delete group" are rejected while the group is locked by a current/future group ticket.
- `individual-tickets`: "Create ticket" identifiers must be unique across individual and group tickets; "Delete ticket" rejects constituent tickets of a group ticket.
- `user-management`: "Command format and responses" lists `GROUPTICKET CREATE`'s prescribed text.

## Impact

- **Code**: `Ticket` becomes abstract; new `IndividualTicket`, `GroupTicket`, `GroupTicketRepository`; `TicketService` manages both kinds; `CommandProcessor` (two commands, lock checks, HELP); `App` wiring. All references to the old concrete `Ticket` move to `IndividualTicket`.
- **Data**: new `grouptickets.txt`; `tickets.txt`, `groups.txt`, `users.txt` unchanged.
- **Out of scope**: attraction admission.
- **Open questions / assumptions**:
  - Constituent tickets are identified `<group ticket id>-<n>` (n = 1..members, in group order) and record the individual base price; the discount applies only to the group ticket total.
  - A purchase is rejected if any generated identifier is already used, if the group has no members, or if the date is before the system date (consistent with individual tickets).
  - Identifiers are unique across individual and group tickets (common abstraction).
  - `TG01` and `TG001` in the source are independent examples, not a naming rule.
  - A group may be deleted once all its group tickets are past; such historical group tickets keep the group identifier.
  - Discounted totals are rounded to two decimals (half up).
