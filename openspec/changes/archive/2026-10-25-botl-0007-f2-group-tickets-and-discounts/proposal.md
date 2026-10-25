# Proposal

## Why

Groups need to buy admission jointly with volume discounts. A group purchase must generate one individual ticket per member at that moment, keep the group stable while its tickets are valid, and protect the members from deletion, introducing composition and a ticket specialization.

## What Changes

- `GROUPTICKET CREATE;<id>;<groupId>;<yyyy-MM-dd>` → `OK GROUP TICKET CREATED -> <total>`: one internal individual ticket per current member at the current individual price, and a total equal to their sum minus the discount for the member count: fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%.
- The composition is frozen at purchase time; later membership changes do not alter the ticket.
- A group with any group ticket dated on or after the system date is locked: no `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE`; it unlocks when all its group tickets are past.
- The generated individual tickets block deletion of their users while current or future (same rule as individual tickets).
- `GROUPTICKET DELETE;<id>`: only when its date is strictly later than the system date; deletes the group ticket and all its internal individual tickets.
- Individual and group tickets share a common abstraction (`Ticket`) and one identifier namespace.
- Group tickets persist with their composition; `HELP` and `uml/model.puml` are updated (inheritance, aggregation with the group, composition of individual tickets).

## Capabilities

### New Capabilities
- `group-ticketing`: Group tickets, discount brackets, frozen composition, group locking, refund rule, persistence.

### Modified Capabilities
- `ticketing`: "Ticket data" becomes the common ticket abstraction shared by individual and group tickets with a single identifier namespace; "Refund individual ticket" excludes tickets that belong to a group ticket.
- `command-processing`: the `HELP` requirement lists the GROUPTICKET commands (the command list moves into its scenario).

## Impact

- `Ticket` becomes abstract with subclasses `IndividualTicket` (former `Ticket`) and `GroupTicket`; `TicketManager`, `TicketRepository` (new `data/grouptickets.txt`), `CommandProcessor` (new commands, group lock checks) and `App`; `uml/model.puml`.
- New tests for every discount boundary, locking and refunds.
