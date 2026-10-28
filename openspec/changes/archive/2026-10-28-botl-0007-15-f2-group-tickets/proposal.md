# Proposal

## Why

Milestone F2-C04 lets non-empty groups buy a joint ticket with `GROUPTICKET CREATE`, composed of one individual ticket per member (identifier `<groupTicketIdentifier>:<dni>`), priced from the current system price with a member-count discount, and adds `GROUPTICKET DELETE` and `GROUP DELETE`. Active group tickets lock the group's composition (GROUP ADD/REMOVE/DELETE) and block deletion of their members.

## What Changes

- Add the group ticket type, reusing the common ticket abstraction: reference to the purchasing group (nullable once the group is deleted), immutable composition of individual tickets, one per member at purchase time, no parallel user collection.
- Add `GROUPTICKET CREATE;<identifier>;<group>;<date>`: existing non-empty group, date not in the past, unique identifier and derived identifiers checked in the shared ticket identifier space with no partial creation; discount brackets 0% (<3), 10% (3–5), 20% (6–10), 50% (>10) applied to each individual ticket's base price; output shows the total.
- Add `GROUPTICKET DELETE;<identifier>`: only when the date is strictly later than the current date; deletes the composition too.
- Add `GROUP DELETE;<Identifier>`: rejected while the group has an active group ticket; historical group tickets are kept with a null group reference.
- Derive group modifiability from active group tickets: GROUP ADD, GROUP REMOVE and GROUP DELETE are rejected while one exists. User deletion already traverses composed tickets and checks group modifiability.
- Users gain a DNI attribute, required to derive composed ticket identifiers.
- Extend `HELP`.
- Commit with the exact message `[F2-C04] Add group ticket purchase`.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase, composition, discount pricing and deletion.

### Modified Capabilities
- `user-groups`: Group modifiability now depends on active group tickets; adds `GROUP DELETE`.
- `execution-control`: HELP lists `GROUP DELETE`, `GROUPTICKET CREATE` and `GROUPTICKET DELETE`.

## Impact

- `GroupTicket` model class, `User` DNI attribute, controller operations, three commands, tests.
- **Flagged ambiguities / ordering conflicts:**
  - The user model with DNI (F1-C01), its validation (F1-C02) and `USER CREATE` (F1-C03) come later in this batch. Only a DNI attribute is added here (no validation); until `USER CREATE` exists, members can only be registered programmatically.
  - Rounding of discounted prices is unspecified; individual prices are rounded to cents (HALF_UP). All bracket percentages yield exact cents for two-decimal base prices except 50% of an odd cent amount.
  - Output texts are not specified; creation prints `GROUPTICKET CREATED. TOTAL PRICE: <total>`.
