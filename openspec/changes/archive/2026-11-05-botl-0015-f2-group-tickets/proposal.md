# Proposal

## Why

Milestone F2-C04 (Delivery 2, step 4) lets non-empty groups buy a joint ticket with `GROUPTICKET CREATE` for a date that is not in the past. The group ticket is composed of one individual ticket per member, with identifiers `<groupTicketIdentifier>:<dni>`, priced from the current system price with a member-count discount. `GROUPTICKET DELETE` deletes future group tickets together with their composition. Active group tickets lock the group against `GROUP ADD`/`GROUP REMOVE`/`GROUP DELETE` and block `USER DELETE` for their members (source §5, §7.3, §8, §9). The previous milestones already route every one of those checks through derived queries that return "no group tickets"; this change gives them real group tickets.

## What Changes

- Add a group ticket type that extends the common ticket abstraction. It holds a fixed composition of individual tickets (one per member at purchase time, each referencing its user) and a one-way reference to the purchasing group, which is cleared when the group is later deleted. It keeps no parallel list of users, and its total price is the sum of its composition's prices.
- Add `GROUPTICKET CREATE;<identifier>;<group>;<date>`. The group must exist and be non-empty, the date must be valid and not earlier than the system date, and the group ticket identifier and every derived `<identifier>:<dni>` must be unused. If any check fails, nothing is created. Each component is priced at the current base price minus the bracket discount (fewer than 3: 0%, 3–5: 10%, 6–10: 20%, more than 10: 50%), and the response shows the total charged.
- Add `GROUPTICKET DELETE;<identifier>`: allowed only when the date is strictly later than the system date. It also deletes the composition tickets.
- Derive the group lock and composition membership from the stored tickets. `GROUP ADD`/`GROUP REMOVE` are rejected while the group has an active group ticket, and `GROUP DELETE` while it has one dated today or later. `TICKET DELETE` rejects composition tickets, and `USER DELETE` rejects members of locked groups and users with active composition tickets.
- `GROUP DELETE` keeps historical group tickets and clears their group reference.
- List the group ticket commands in `HELP`.

In scope: group tickets, discounts, atomic creation, deletion, and the derived locks.

Out of scope: attraction access with group tickets (later milestone) and ticket persistence.

## Capabilities

### New Capabilities
- `group-tickets`: Group ticket purchase and deletion — composition, derived identifiers, member-count discounts, atomic creation, and history retention.

### Modified Capabilities
- `user-groups`: adds the requirements that an active group ticket locks the group's composition and deletion, and that deleting a group keeps its historical group tickets.

## Impact

- New `model.GroupTicket`; `ParkController` implements `hasActiveGroupTicket`, `isPartOfGroupTicket` and group-ticket traversal; `TicketCommands` gains the `GROUPTICKET` commands; `GroupCommands` clears historical references on deletion; tests.
