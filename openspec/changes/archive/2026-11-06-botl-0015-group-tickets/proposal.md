# Proposal

## Why

Milestone F2-C04 (Delivery 2, step 4) lets non-empty groups buy a joint ticket with `GROUPTICKET CREATE`, composed of one individual ticket per member with derived identifiers and a size-based discount on the current base price, and adds `GROUPTICKET DELETE`. With group tickets in place, group locking (`GROUP ADD`/`REMOVE`/`DELETE`) and the `USER DELETE` checks become effective for real.

## What Changes

- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: unique identifier, existing non-empty group, date ≥ current date; one individual ticket per member at purchase time with identifier `<groupTicketIdentifier>:<dni>`; every derived identifier checked in the shared identifier space before anything is created (no partial creation); discount by member count (<3: 0%, 3–5: 10%, 6–10: 20%, >10: 50%) applied to the current base price of each constituent ticket; total = sum of constituents, printed.
- The group ticket references its group while it exists (reference cleared when the group is deleted) and keeps its own identifier, date, price and composition, which never changes afterwards; it holds no parallel user collection.
- `GROUPTICKET DELETE;<identifier>`: only when dated strictly after the current date; deletes its composition too. Constituent tickets cannot be deleted with `TICKET DELETE`.
- Group locking now uses the group's active group tickets; `GROUP DELETE` keeps historical group tickets with a cleared group reference.
- `USER DELETE` checks include the constituent tickets of active group tickets (already covered by the ticket traversal).
- `HELP` lists the group-ticket commands.

### In scope
- Source sections 5, 7.3, 8 and 9 (as they apply to group tickets).

### Out of scope
- Attractions and access (F2-C05 onwards), persistence of groups and tickets (F2-C08).

## Capabilities

### New Capabilities
- `group-tickets`: group ticket purchase, composition, identifiers, discounts and deletion.

### Modified Capabilities
- `user-groups`: group deletion retains historical group tickets with a cleared group reference.
- `execution-control`: `HELP` lists `GROUPTICKET CREATE` and `GROUPTICKET DELETE`.

## Impact

- `Ticket` gains base price + discount price behavior; new `GroupTicket`, `GroupDiscount`; `TicketService` and `ParkController` extended; tests.
