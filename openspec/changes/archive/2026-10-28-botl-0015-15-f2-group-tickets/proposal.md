# Proposal

## Why

Milestone F2-C04: non-empty groups must be able to buy a joint ticket for all current members on a non-past date, composed of one individual ticket per member with derived identifiers, discounted according to the number of members, and deletable only when strictly in the future. With group tickets in place, the derived group lock (GROUP ADD/REMOVE/DELETE) and the USER DELETE checks become effective.

## What Changes

- Add the group ticket type, extending the common ticket abstraction: composition of individual tickets (one per member at purchase time, identifiers `<groupTicketIdentifier>:<dni>`, each referencing its user), a reference to the purchasing group (cleared when the group is deleted), total price = sum of its individual tickets.
- Discounts by number of members: fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%, applied to the current base price of each individual ticket.
- Add `GROUPTICKET CREATE;<identifier>;<group>;<date>`: existing non-empty group, date ≥ current date, identifier and every derived identifier free in the shared ticket space (all-or-nothing); prints the total charged.
- Add `GROUPTICKET DELETE;<identifier>`: only when the date is strictly later than the current date; deletes the composition too.
- Feed the derived group lock with real group tickets: a group is locked while it has a group ticket dated on or after the current date. `GROUP DELETE` keeps historical group tickets, clearing their group reference.
- `USER DELETE` now also sees composition tickets and locked groups (wired in F2-C03, effective now).
- `TICKET DELETE` rejects group-ticket identifiers and composition tickets; `HELP` lists the group-ticket commands.
- Commit with the mandatory message `[F2-C04] Add group ticket purchase`.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase and deletion, composition and derived identifiers, discounts, the group reference and the effect on group locking and historical records.

### Modified Capabilities
- `execution-control`: `HELP` also lists `GROUPTICKET CREATE` and `GROUPTICKET DELETE`.

## Impact

- New `model.GroupTicket`; `IndividualTicket` gains a discount-aware price; `TicketService` gains group-ticket operations and the active-group-ticket query; `ParkController` wires it as the group lock and orchestrates `GROUP DELETE` and `GROUPTICKET CREATE`.
- Ambiguities flagged:
  - Discounted prices are rounded to cents (HALF_UP); the total is the sum of rounded individual prices.
  - Output: `GROUPTICKET CREATED. Total price: <amount> EUR`, `GROUPTICKET DELETED`.
  - A group may buy several group tickets (for different or the same dates); nothing in the statement forbids it.
