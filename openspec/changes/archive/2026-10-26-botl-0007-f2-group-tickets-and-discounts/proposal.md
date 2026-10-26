# Proposal

## Why

Groups created in the previous delivery cannot buy admission together. Families and schools need a single purchase for all members with a size-based discount, while the system must keep each member's admission traceable and prevent changes to groups and users that hold valid tickets.

## What Changes

- New command `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`: buys a group ticket for a date; generates one individual ticket per current member at the current individual price; total = sum of individual prices with a member-count discount (fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%); answers `OK GROUP TICKET CREATED -> <Precio>`.
- The group composition at purchase time is kept in the group ticket; later group changes do not alter it.
- New command `GROUPTICKET DELETE;Id`: allowed only when the ticket date is after the system date; also deletes its internal individual tickets.
- A group with any group ticket dated on or after the system date is locked: no GROUP ADD, GROUP REMOVE or GROUP DELETE. Locks lift as the system date passes the ticket date.
- Internal individual tickets block deletion of their users exactly like ordinary tickets.
- Individual and group tickets share a common ticket abstraction (identifier, date, price); identifiers are unique across all tickets.
- Group tickets are persisted and restored; HELP and the PlantUML model (inheritance, aggregation with the group, composition with internal tickets) are updated.

## Capabilities

### New Capabilities

### Modified Capabilities
- `ticket-management`: group ticket purchase and deletion, discounts, internal tickets, unique identifiers across ticket kinds, persistence of group tickets; TICKET DELETE does not delete internal tickets of a group ticket.
- `group-management`: groups with current or future group tickets cannot be modified or deleted.
- `command-processing`: HELP lists GROUPTICKET CREATE and GROUPTICKET DELETE.

## Impact

- `Ticket` becomes an abstract base with subclasses `IndividualTicket` (previous ticket class) and `GroupTicket`; `TicketManager`/`TicketStorage` handle both; new data file `data/group_tickets.txt`.
- `CommandProcessor` routes `GROUPTICKET` and checks the group lock before group changes.
- `docs/uml/model.puml` updated.
