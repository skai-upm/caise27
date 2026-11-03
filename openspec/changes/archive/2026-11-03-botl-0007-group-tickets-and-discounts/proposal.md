# Proposal

## Why

Groups need to buy tickets jointly, at a discount depending on their size, with the purchased composition frozen and the group protected from changes while the tickets are valid. This also introduces ticket specialisation and composition into the model.

## What Changes

- New `GroupTicket`, composed of one individual `Ticket` per group member at purchase time, aggregated with its `Group`; individual and group tickets share the abstraction `ParkTicket`.
- `GROUPTICKET CREATE;TG01;G01;2026-10-15`: date on or after the system date, non-empty group, unused id; total = individual price × members with discount 0% (<3), 10% (3–5), 20% (6–10), 50% (>10); responds `OK GROUP TICKET CREATED -> <Precio>`.
- `GROUPTICKET DELETE;TG001`: only when the date is later than the system date; deletes its constituent tickets too.
- A group with a group ticket dated on or after the system date is locked: `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` are rejected. Constituent tickets block deleting their users like ordinary tickets.
- Group tickets persisted in `grouptickets.txt`; HELP and `uml/model.puml` updated.
- No attraction admission.

## Capabilities

### New Capabilities
- `group-tickets`: group-ticket purchase, discounts, composition, deletion and the GROUPTICKET command contract.
- `group-ticket-file-persistence`: storage and restoration of group tickets with their constituent tickets.

### Modified Capabilities
- `user-groups`: adds the lock on membership changes and deletion while the group has a current or future group ticket.
- `application-control`: HELP lists `GROUPTICKET CREATE` and `GROUPTICKET DELETE`.
- `domain-model-diagram`: the diagram also shows the ticket inheritance, the composition and the group aggregation.

## Impact

- New `ParkTicket`, `GroupTicket`, `GroupTicketFileStorage`; `Ticket` extends `ParkTicket`; `TicketService` manages group tickets too (needs `GroupService` to restore them); `CommandProcessor` (new commands, group lock check); `App` wiring; `uml/model.puml`.
- New runtime file `grouptickets.txt` (git-ignored). Existing file formats unchanged.
