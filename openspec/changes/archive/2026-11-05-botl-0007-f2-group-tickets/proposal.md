# Proposal

## Why

Milestone F2-C04 lets non-empty groups buy a joint ticket (`GROUPTICKET CREATE`) made of one individual ticket per current member, with identifiers `<groupTicketIdentifier>:<dni>`, a member-count discount on the current base price, and `GROUPTICKET DELETE` for future group tickets. Active group tickets lock their group, and user deletion must also respect group tickets and locked groups.

## What Changes

- Add group tickets as a new ticket type reusing the common ticket abstraction. Each one is composed of individual tickets, one per member at purchase time, with derived identifiers `<id>:<dni>`, and holds a nullable reference to its group.
- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: the group must exist and be non-empty, the date must not be in the past, and the identifier plus every derived identifier must be free in the shared identifier space. Creation is all-or-nothing. The total price after the discount is displayed.
- Discount per individual ticket by member count: fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%. The total is the sum of the component prices.
- `GROUPTICKET DELETE;<identifier>`: only when the date is strictly after the current system date; it deletes its component tickets too.
- `TICKET DELETE` rejects tickets that belong to a group ticket (**MODIFIED**).
- Group lock is now computed from the system's real group tickets (the `user-groups` rules are unchanged). Deleting a group sets the group reference of its historical group tickets to null, and their identifier, date, price and composition are kept.
- User-deletion guard (**MODIFIED**): it also rejects deletion when the user belongs to a locked group. When deletion is allowed, the user is removed from all their groups.
- `HELP` lists the GROUPTICKET commands.

### Out of scope / deferred
- The `USER DELETE` command and the user model don't exist yet (batch ordering). DNIs come from central management's user lookup, which is empty until user registration exists.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `tickets`: adds group ticket purchase, discounts and deletion. Modifies individual ticket deletion (composition members can't be deleted directly) and the user-deletion rule (locked groups, removal from groups).

## Impact

- New `GroupTicket` and `GroupTicketCommands`.
- Changes to `IndividualTicket` (containing group ticket), `ParkController` (group tickets, lock source, user-deletion guard), `UserDirectory` (`dniOf`), `HELP` and tests.
