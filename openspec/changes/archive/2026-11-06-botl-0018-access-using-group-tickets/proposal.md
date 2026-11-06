# Proposal

## Why

Milestone F2-C07 (Delivery 2, step 7) extends `ATTRACTION ENTER` to group tickets — all members of the purchase enter together or nobody does, within the attraction's capacity — and adds the operations needed to verify entries and exits: `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## What Changes

- `ATTRACTION ENTER` accepts group tickets through the same access operation: all users of the group ticket's composition enter together; if capacity is insufficient for all of them (or any is already inside), none enters.
- `ATTRACTION EXIT;AttractionIdentifier;Email`: the user with that email leaves the attraction, releasing one place; members who entered with a group ticket leave individually.
- `ATTRACTION STATUS;AttractionIdentifier`: shows identifier, name, maximum capacity, users inside, available places and the users currently inside, from the attraction's own state, regardless of how they entered.
- `HELP` lists the new commands.

### In scope
- Source sections 10 (group tickets), 11 and 12.

### Out of scope
- Persistence of attractions and occupants (F2-C08).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-access`: entry with group tickets, indivisible.
- `attractions`: leaving an attraction and checking its status.
- `execution-control`: `HELP` lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## Impact

- `TicketService.findForAccess` (any top-level ticket); `AttractionService.exit/status`; `CommandProcessor` dispatch; tests.
