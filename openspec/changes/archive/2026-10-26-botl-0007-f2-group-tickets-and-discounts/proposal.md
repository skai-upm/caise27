# Proposal

## Why

Groups need to buy admission jointly with volume discounts. A group purchase must produce the members' individual tickets, keep the group's composition stable while the purchase is valid, and be handled together with individual tickets through a common ticket abstraction.

## What Changes

- New command `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`: generates one individual ticket per current member at the current individual price and charges the sum minus a size-based discount (fewer than 3: 0%, 3–5: 10%, 6–10: 20%, more than 10: 50%); response `OK GROUP TICKET CREATED -> <Precio>`.
- New command `GROUPTICKET DELETE;Id`: allowed only for group tickets dated strictly after the system date; also deletes its internal individual tickets.
- While a group has a group ticket dated on or after the system date, the group is locked: members cannot be added or removed and the group cannot be deleted.
- Internal individual tickets block the deletion of their users like ordinary tickets, and cannot be deleted on their own with `TICKET DELETE`.
- Individual and group tickets share a common ticket abstraction; group tickets aggregate their group and are composed of their individual tickets.
- Group tickets are persisted and restored; new commands appear in `HELP`; the PlantUML model shows the hierarchy.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase, discounts, internal individual tickets, deletion and persistence.

### Modified Capabilities
- `user-groups`: groups become locked by current or future group tickets.
- `tickets`: individual tickets that belong to a group ticket cannot be deleted individually.
- `uml-model`: the model must show the ticket hierarchy, aggregation with groups and composition of individual tickets.

## Impact

- `Ticket` becomes abstract with subclasses `IndividualTicket` and `GroupTicket`; `TicketManager`, `TicketRepository`, `GroupManager`, `CommandProcessor`, `App` and `uml/model.puml` updated.
- New runtime file `data/grouptickets.txt`.
