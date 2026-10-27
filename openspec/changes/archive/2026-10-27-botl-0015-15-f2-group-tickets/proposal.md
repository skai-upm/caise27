# Proposal

## Why

Milestone F2-C04 lets non-empty groups buy a joint ticket for a future (or current) date with a size-based discount, adds deletion of future group tickets, and makes group modification, group deletion and user deletion respect the locks created by active group tickets.

## What Changes

- `GROUPTICKET CREATE;<identifier>;<group>;<date>`: non-empty existing group, date not in the past, unique identifier; composed of one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`; all identifiers checked before anything is created; each composed ticket costs the current base price minus the size discount (0% <3, 10% 3–5, 20% 6–10, 50% >10); displays the total charged.
- The group ticket references its group while it exists (reference cleared when the group is deleted); composition never changes after purchase.
- `GROUPTICKET DELETE;<identifier>`: only when dated strictly after the system date; deletes its composed tickets too.
- GROUP ADD / GROUP REMOVE / GROUP DELETE are rejected while the group has an active group ticket (derived from tickets and the system date).
- USER DELETE is rejected when the user has an active composed ticket or belongs to a locked group.
- HELP lists the GROUPTICKET commands.
- Record the mandatory milestone commit `[F2-C04] Add group ticket purchase` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `ticket-management`: adds group ticket purchase, discount, composition and deletion.
- `group-management`: group modifiability now actually depends on active group tickets; historical group tickets survive group deletion.
- `user-management`: USER DELETE also blocked by membership in a locked group.
- `command-processing`: HELP lists GROUPTICKET commands.

## Impact

- New `GroupTicket`; `IndividualTicket` knows its containing group ticket; `TicketService` group ticket operations; `ParkController.hasActiveGroupTicket` implemented; `GroupService.deleteGroup` clears references; `UserService.deleteUser` checks locked groups; `CommandProcessor` dispatch and HELP.
