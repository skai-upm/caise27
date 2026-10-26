# Proposal

## Why

Groups created in the previous change need to buy admission together. Group purchases introduce composition (a group ticket made of members' individual tickets), ticket specialization, volume discounts and locking rules tied to the system date.

## What Changes

- `GROUPTICKET CREATE;<id>;<groupId>;<yyyy-MM-dd>` buys a group ticket: one individual ticket is generated for each member at the moment of purchase, at the current individual price; the total is the sum of those prices minus a discount by member count (fewer than 3: 0%, 3–5: 10%, 6–10: 20%, more than 10: 50%). Prints `OK GROUP TICKET CREATED -> <total>`.
- `GROUPTICKET DELETE;<id>` deletes a group ticket and all its internal individual tickets, only if its date is strictly after the system date.
- A group with any group ticket dated on or after the system date is locked: members cannot be added or removed and the group cannot be deleted. The lock lifts automatically when all its group tickets are in the past.
- Generated individual tickets block deletion of their users like any other current/future ticket; they cannot be deleted on their own with `TICKET DELETE`.
- Individual and group tickets share a common abstraction (`Ticket`) and a single identifier space.
- Group tickets persist between executions; the PlantUML model shows the ticket hierarchy, the aggregation with the group and the composition with the internal tickets.

## Capabilities

### New Capabilities
- `group-tickets`: buying and deleting group tickets, discount brackets, internal ticket generation, group locking.

### Modified Capabilities
<!-- None: user deletion already blocks on any current/future ticket; individual ticket rules are unchanged for standalone tickets. -->

## Impact

- `Ticket` becomes abstract with subclasses `IndividualTicket` (former `Ticket`) and `GroupTicket`.
- `TicketManager`, `TicketRepository` (new `data/grouptickets.txt`; individual ticket lines gain an optional owning group-ticket field, old lines still load), `CommandProcessor`, `App`, `uml/model.puml` updated.
