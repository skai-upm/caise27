# Proposal

## Why

Groups exist but can only enter as individual buyers. Delivery 2 lets a group buy one ticket for all its members with a size-based discount, which introduces ticket specialisation (individual vs group), composition (a group ticket is made of its members' individual tickets) and date-dependent locking rules on groups and users.

## What Changes

- New command `GROUPTICKET CREATE;<id>;<groupId>;<yyyy-MM-dd>`: buys a group ticket for the group's current members, generating one individual ticket per member, and prints `OK GROUP TICKET CREATED -> <total>`.
- Price: the current individual price is the base; the sum for all members gets a discount by member count — fewer than 3: 0%, 3–5: 10%, 6–10: 20%, more than 10: 50%.
- The composition is a snapshot of the members at purchase time.
- **Group lock**: while a group has a group ticket dated on or after the system date, members cannot be added or removed and the group cannot be deleted. The lock lifts automatically when all its group tickets are in the past.
- The generated individual tickets refer to their users and, like ordinary tickets, prevent those users from being deleted while current or future.
- New command `GROUPTICKET DELETE;<id>`: allowed only if the date is later than the system date; deletes the group ticket and all its internal individual tickets. Internal tickets cannot be deleted on their own with `TICKET DELETE`.
- Individual and group tickets share a common ticket abstraction (identifier, date, price, users covered) and a single identifier space.
- Group tickets are persisted and restored; deleting a group (when unlocked) removes its past group tickets, and deleting a user removes their past internal tickets from group compositions.
- HELP lists the new commands; the PlantUML model shows the inheritance, aggregation, composition and multiplicities.

## Capabilities

### New Capabilities
<!-- None: group tickets extend the existing ticket capability. -->

### Modified Capabilities
- `ticket-management`: adds group ticket purchase, discount brackets, composition, deletion and persistence, the common ticket abstraction and shared identifier space; individual deletion now rejects internal tickets.
- `group-management`: adds the group lock while current/future group tickets exist, and removal of a deleted group's past group tickets.

## Impact

- `Ticket` becomes an abstract base with `IndividualTicket` and new `GroupTicket`; `TicketRepository` also stores group tickets (`data/grouptickets.txt`) and the owning group ticket of internal tickets; `TicketManager` gains group ticket rules and lock checks; `CommandProcessor` adds the GROUPTICKET commands and checks locks before group changes.
- `uml/model.puml` updated. Tests for every bracket boundary, locking, deletion and persistence.
