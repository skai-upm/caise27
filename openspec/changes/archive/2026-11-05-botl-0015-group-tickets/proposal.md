# Proposal

## Why

Milestone F2-C04 lets non-empty groups buy joint tickets. A group ticket is composed of one individual ticket per member (identifier `<groupTicket>:<dni>`), priced from the current system price with a size-based discount, and it locks the group's composition while it is active. The same milestone adds `GROUPTICKET DELETE` and `GROUP DELETE`, and extends the `USER DELETE`, `GROUP ADD` and `GROUP REMOVE` checks to account for group tickets.

## What Changes

- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: non-empty existing group, date ≥ current date; creates one individual ticket per current member with derived identifiers, all checked in the shared identifier space before anything is stored (no partial creation); applies the discount (fewer than 3: 0%, 3–5: 10%, 6–10: 20%, more than 10: 50%) to each individual ticket's base price; prints the total price.
- The group ticket references its group (reference cleared if the group is later deleted) and its fixed composition; it does not keep a parallel user list; groups and users do not reference tickets.
- `GROUPTICKET DELETE;<identifier>`: only when its date is strictly after the current date; deletes its composition too.
- `TICKET DELETE` refuses individual tickets that belong to a group ticket.
- `GROUP DELETE;<identifier>`: only when the group has no active group ticket; historical group tickets are kept.
- `GROUP ADD`/`GROUP REMOVE` are refused while the group has an active group ticket (locked state derived from tickets and the current date).
- `USER DELETE` is refused when the user has an active ticket in a group ticket composition or belongs to a locked group.
- Out of scope: attraction access (F2-C05+), persistence (F2-C08).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `tickets`: adds group tickets (creation, discounts, deletion) and protection of composition tickets from `TICKET DELETE`.
- `user-groups`: adds `GROUP DELETE` and the locking of groups by active group tickets.
- `user-deletion`: adds the locked-group rule.

## Impact

- New `model.GroupTicket`, `model.GroupDiscount`; extensions to `TicketRepository`, `TicketService`, `GroupService`, `UserService`, `CommandProcessor`, HELP.
