# Proposal

## Why

Milestone F2-C04 adds group ticket purchase: non-empty groups buy a ticket composed of one individual ticket per member (identifiers `<groupTicketIdentifier>:<dni>`), priced from the current system price with a member-count discount, plus GROUPTICKET DELETE. With group tickets in place, the group lock rules (GROUP ADD / REMOVE / DELETE) and the user-deletion check become effective.

## What Changes

- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: for an existing, non-empty group and a date not in the past; creates one individual ticket per current member with identifier `<identifier>:<dni>`, all identifiers checked in the shared ticket identifier space before anything is created; applies the discount bracket (fewer than 3: 0 %, 3–5: 10 %, 6–10: 20 %, more than 10: 50 %) to the current base price of each individual ticket; displays the total charged (sum of the individual prices).
- The group ticket references its purchasing group (unidirectional) while the group exists; its composition never changes after purchase and is not shared; it keeps no parallel user collection.
- `GROUPTICKET DELETE;<identifier>`: only when the date is strictly after the current date; deletes its composition with it.
- `TICKET DELETE` rejects individual tickets that belong to a group ticket (and group ticket identifiers).
- GROUP ADD / GROUP REMOVE / GROUP DELETE now actually detect active group tickets; deleting a group with only historical group tickets clears those tickets' group reference while keeping identifier, date, price and composition.
- The user-deletion check also covers individual tickets inside active group tickets and blocks deletion when the user belongs to a group locked by an active group ticket.
- `User` gains a DNI (needed for derived identifiers).
- HELP lists both GROUPTICKET commands.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `tickets`: group tickets (creation, discount, composition, deletion), composed tickets not deletable individually, extended user-deletion check.
- `user-groups`: group deletion keeps historical group tickets with a null group reference.
- `command-interface`: HELP lists the GROUPTICKET commands.

## Impact

- New `GroupTicket`; changes to `User`, `Ticket` registry operations in `ParkController`, `CommandProcessor`; tests updated for `User` with DNI.

## Ambiguities and unmet prerequisites (flagged)

- **DNI:** the user model with DNI and its validation belong to Delivery 1 milestones not yet applied. Only a DNI attribute is added to `User` (no validation). Users are still registered programmatically; through commands GROUPTICKET CREATE works only for groups whose members exist, which currently requires programmatic registration.
- **USER DELETE** still does not exist; the extended restriction is provided as `canDeleteUser(User)` for that milestone to use.
- **Rounding:** discounted prices are rounded to 2 decimals, half-up (e.g. 45.55 × 0.90 = 40.995 → 41.00); the source does not specify rounding.
- A member whose derived identifier collides with another (e.g. two users with the same DNI) makes the purchase fail as an identifier conflict; nothing is created.
- Output texts are not specified; `GROUP TICKET CREATED. TOTAL PRICE: <amount> EUR` and `GROUP TICKET DELETED` are used.
