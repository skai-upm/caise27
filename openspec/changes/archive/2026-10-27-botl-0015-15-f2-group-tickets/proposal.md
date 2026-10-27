# Proposal

## Why

Milestone F2-C04 lets non-empty groups buy a joint ticket composed of one individual ticket per member, with a size-based discount, and introduces the locking rules that depend on active group tickets: a group with an active group ticket cannot change its composition or be deleted, and users in such groups cannot be deleted.

## What Changes

- Add group tickets, a second ticket type extending the common ticket abstraction: composed of individual tickets (one per member at purchase time, identifiers `<groupTicketId>:<dni>`), holding a nullable reference to the purchasing group, price = sum of its components.
- Add the discount brackets (fewer than 3 members 0%, 3–5 10%, 6–10 20%, more than 10 50%) applied to the current base price of each component ticket, reusing the common ticket price behaviour.
- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: unique identifier, all derived identifiers free in the shared identifier space (no partial creation), existing non-empty group, date ≥ current date; response shows the total price.
- `GROUPTICKET DELETE;<identifier>`: only when the date is strictly after the current date; its component tickets are deleted with it.
- `TICKET DELETE` rejects tickets that are components of a group ticket.
- A group is locked while it has an associated active group ticket (date ≥ current date), derived on demand: GROUP ADD, GROUP REMOVE and GROUP DELETE are rejected while locked. Deleting a group leaves its historical group tickets with a null group reference.
- `USER DELETE` is rejected when the user belongs to a locked group (active-ticket check already covers component tickets through the common abstraction).
- Commit with the exact message `[F2-C04] Add group ticket purchase`.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase, composition, discounts, deletion and group locking.

### Modified Capabilities
- `tickets`: the shared identifier space now includes derived component identifiers; TICKET DELETE rejects component tickets.
- `user-groups`: GROUP ADD/REMOVE/DELETE are restricted by active group tickets; group deletion keeps historical group tickets.
- `user-deletion`: USER DELETE is rejected when the user belongs to a locked group.

## Impact

- `model/Ticket` gains a discount rate used by `getPrice()`; new `model/GroupTicket`; `TicketRepository` identifier check covers components; `ParkController` gains `isGroupLocked`; new GROUPTICKET commands; HELP updated.
- Ambiguities flagged: prices are rounded to two decimals (half-up) per component ticket; response is `GROUPTICKET CREATED - Total price: <amount> EUR`; GROUP DELETE was already available from F2-C02 and is extended here with the locking rule.
