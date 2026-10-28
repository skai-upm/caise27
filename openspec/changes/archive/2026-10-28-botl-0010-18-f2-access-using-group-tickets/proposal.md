# Proposal

## Why

Milestone F2-C07 requires the same `ATTRACTION ENTER` operation to accept group tickets atomically (all members of the composition enter together or none does, respecting capacity), and requires consulting attraction status so the results of entry and exit can be verified. The linked source sections also define `ATTRACTION EXIT` (leaving and releasing capacity) and `ATTRACTION STATUS`.

## What Changes

- Specify and verify group-ticket access through the existing common access operation: every user of the composition enters together; insufficient capacity means nobody enters.
- Add `ATTRACTION EXIT;AttractionIdentifier;Email`: removes that user from the attraction's occupants, releasing capacity; members who entered with a group ticket leave individually.
- Add `ATTRACTION STATUS;AttractionIdentifier`: shows identifier, name, maximum capacity, number of users inside, available places and the users inside, computed from the attraction's own state and independent of how users entered.
- Extend `HELP`.
- Commit with the exact message `[F2-C07] Add group attraction access`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attraction-access`: adds group-ticket access, ATTRACTION EXIT and ATTRACTION STATUS requirements.
- `execution-control`: HELP lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## Impact

- Controller `exitAttraction`, two commands, tests; access code unchanged (already on the common abstraction).
- **Flagged ambiguities:**
  - The status output format is left to the implementer by the source; a labelled multi-line block is used.
  - Exiting by an e-mail of a user not inside the attraction (or an unknown attraction) is rejected with an error message.
  - Users inside the attraction are listed by e-mail in STATUS (as in ATTRACTION LIST).
