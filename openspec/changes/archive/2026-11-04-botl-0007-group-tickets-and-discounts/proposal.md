# Proposal

## Why

Groups can be formed, but only individuals can buy tickets. Groups must be able to buy a joint ticket for a date. The ticket is composed of one individual ticket per member at purchase time, has a member-count discount, and locks the group and its members while it is current or future.

## What Changes

- Introduce a common ticket abstraction. The abstract `Ticket` has an id, a date and a price. It has two subclasses: `IndividualTicket` (today's ticket, renamed) and `GroupTicket`.
- New command `GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd` (e.g. `GROUPTICKET CREATE;TG01;G01;2026-10-15`):
  - It creates one constituent `IndividualTicket` per member at the current individual price, using the group composition at that moment.
  - It applies the discount to the sum: fewer than 3 members 0%, 3–5 10%, 6–10 20%, more than 10 50%.
  - On success it replies exactly `OK GROUP TICKET CREATED -> <Precio>`.
- New command `GROUPTICKET DELETE;IdTicket` (e.g. `GROUPTICKET DELETE;TG001`), allowed only when the date is later than the system date. It deletes the group ticket together with all its constituent tickets.
- **Group lock:** while a group has any group ticket dated on or after `SYSTEM DATE`, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it are rejected.
- Constituent tickets block deletion of their users exactly like ordinary individual tickets (date ≥ system date).
- Later membership changes never alter a purchased group ticket.
- Group tickets persist in `group-tickets.txt`; the `tickets.txt` format is unchanged.
- `HELP` and the PlantUML model (inheritance, aggregation, composition) are updated.

## Capabilities

### New Capabilities
- `group-tickets`: Group-ticket purchase, composition, discount brackets, deletion, group lock and member-deletion protection.

### Modified Capabilities
- `individual-tickets`: "Ticket deletion" — constituent tickets of a group ticket cannot be deleted with `TICKET DELETE`; "Ticket creation" — identifiers are unique across individual, group and constituent tickets.
- `ticket-file-persistence`: group-ticket storage and restoration added.
- `application-control`: "HELP command" lists the group-ticket commands.
- `uml-model`: "PlantUML class model" shows the ticket hierarchy, aggregation and composition.

## Impact

- New: abstract `Ticket`, `GroupTicket`, `GroupTicketFileStorage`; `Ticket` renamed to `IndividualTicket`.
- Changed: `TicketManager` (group tickets, unified id space, active-ticket checks), `CommandProcessor` (new commands, group-lock checks), `App` (wires `group-tickets.txt`), PlantUML, tests.
- No change to user, group or individual-ticket command syntax or file formats.
