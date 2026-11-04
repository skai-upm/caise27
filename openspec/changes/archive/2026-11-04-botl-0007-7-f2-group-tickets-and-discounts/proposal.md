# Proposal

## Why

Groups exist but cannot buy tickets together. Groups need joint purchases composed of one individual ticket per member at purchase time, priced with member-count discounts, and the purchase must lock the group and its users while the tickets are current or future.

## What Changes

- New group ticket for a date, composed of one individual ticket per member of the group at purchase time; the composition never changes afterwards.
- `GROUPTICKET CREATE;TG01;G01;2026-10-15` creates it and answers exactly `OK GROUP TICKET CREATED -> <Precio>`; the base is the current individual price; the discount on the sum depends on members: fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%.
- `GROUPTICKET DELETE;TG001` deletes a group ticket only when its date is later than the system date, removing its individual tickets together.
- A group with any group ticket dated on or after the system date is locked: `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE` are rejected. The lock ends when all such tickets are in the past.
- Constituent individual tickets block deletion of their users while current or future, exactly like ordinary individual tickets.
- Individual and group tickets share a common ticket abstraction (inheritance); the PlantUML model shows inheritance, composition, aggregation and multiplicities.
- `HELP` lists the new commands. Out of scope: attraction admission.

The source requests the change name `create-group-tickets`; this batch uses the BOTL ID `botl-0007-7-f2-group-tickets-and-discounts`. The source's `TG01` (create) and `TG001` (delete) examples are kept as distinct literal identifiers.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase, composition, discounts, deletion and group locking.
- `group-ticket-file-persistence`: Storage and restoration of group tickets with their constituent tickets.

### Modified Capabilities
- `user-groups`: membership changes and group deletion are rejected while the group is locked by an active group ticket (added requirement).
- `individual-tickets`: `TICKET DELETE` cannot delete a ticket that belongs to a group ticket; identifiers are unique across individual, group and constituent tickets.
- `user-management`: deletion is also blocked by current/future constituent tickets.
- `application-control`: `HELP` lists `GROUPTICKET CREATE` and `GROUPTICKET DELETE`.
- `uml-model`: model shows the ticket hierarchy and group-ticket relationships.

## Impact

- `Ticket` becomes an abstract base; the current concrete class is renamed `IndividualTicket` (no behavior or file-format change); new `GroupTicket`, `GroupTicketFileStore`; `TicketManager` manages both kinds; `CommandProcessor` adds commands and group-lock checks.
- New data file `grouptickets.txt`; `tickets.txt` format unchanged.
- `uml/model.puml` updated.
