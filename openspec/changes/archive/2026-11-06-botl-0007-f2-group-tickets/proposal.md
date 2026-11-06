# Proposal

## Why

Milestone F2-C04 lets a non-empty group buy one joint ticket for all its current members, with a size-based discount on the current system price. It also completes the group and user rules that depend on group tickets: a group is locked while it has an active group ticket, and a user covered by an active group ticket cannot be deleted. Today groups exist and the lock rule is coded, but no group ticket can be created, so the lock never applies.

## What Changes

- Add group tickets as a second ticket type built on the common ticket abstraction. A group ticket is composed of one individual ticket per group member at purchase time, with identifiers `<groupTicketIdentifier>:<dni>`, and keeps a reference to the purchasing group while that group exists.
- Add `GROUPTICKET CREATE;<identifier>;<group>;<date>`. The group must exist and be non-empty, and the date must not be in the past. The group ticket identifier and every derived identifier must be unused in the shared ticket identifier space. Nothing is created if any check fails. The total price is displayed.
- Apply group discounts by member count to the current base price of each composed ticket: fewer than 3 members 0%, 3-5 members 10%, 6-10 members 20%, more than 10 members 50%. The total is the sum of the composed tickets.
- Add `GROUPTICKET DELETE;<identifier>`, allowed only when the group ticket is dated strictly after the current date. It also deletes its composed tickets.
- Composed individual tickets cannot be deleted with `TICKET DELETE`.
- The group lock (GROUP ADD / REMOVE / DELETE) and the user-deletion check now use the real group tickets. When a group is deleted, its historical group tickets are kept and lose their group reference.
- List the GROUPTICKET commands in HELP.

### In scope
- Group ticket model, composition, discount, the two GROUPTICKET commands and the interaction rules with groups and user deletion.

### Out of scope
- The user model and USER DELETE command (later milestones in this batch). The member DNI comes from the central controller's user directory, which the user milestones will provide.
- Persistence of group tickets (complete park persistence milestone).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `tickets`: adds group tickets (creation, composition, discount, deletion, group reference) and their HELP entries. Modifies TICKET DELETE (composed tickets excluded) and the user-deletion requirement (covers tickets composed in group tickets).

## Impact

- New `es.upm.park.model.GroupTicket`. `Ticket` gains the shared discount behavior, and `IndividualTicket` records whether it is part of a group ticket.
- `ParkController`: the user directory (existence + DNI) replaces the existence-only lookup, the group lock is computed from stored group tickets, and group ticket operations are added.
- `CommandProcessor`: GROUPTICKET handlers and HELP.
- Tests updated to use real group tickets instead of stub ticket dates.
