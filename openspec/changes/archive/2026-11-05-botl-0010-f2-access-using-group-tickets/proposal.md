# Proposal

## Why

Milestone F2-C07 requires `ATTRACTION ENTER` to accept group tickets atomically within the attraction's capacity, and requires attraction status to be checkable so entry and exit results can be verified. Leaving attractions (`ATTRACTION EXIT`) and checking their status (`ATTRACTION STATUS`) are the source operations that make this verification possible.

## What Changes

- `ATTRACTION ENTER` with a group ticket admits all users of its component tickets together, or none if capacity is short. It reuses the same access operation and the date and existing-occupant checks as individual tickets.
- Add `ATTRACTION EXIT;AttractionIdentifier;Email`, which removes that user from the attraction and frees their place. Each member who entered with a group ticket leaves individually. Unknown attractions and users not inside are reported as errors.
- Add `ATTRACTION STATUS;AttractionIdentifier`, which shows the identifier, name, maximum capacity, number of users inside, available places and the users inside. It is computed from the attraction's own state, regardless of how the users entered.
- `HELP` lists both new commands.

### Out of scope
- Pass consumption and attraction types (Delivery 3), and persistence.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: adds entry with group tickets, leaving attractions and checking attraction status.

## Impact

- `ParkController.leaveAttraction`.
- The `ATTRACTION EXIT` and `ATTRACTION STATUS` commands.
- `HELP` and tests.
