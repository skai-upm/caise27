# Proposal

## Why

Milestone F2-C07 extends the same access operation to group tickets — all composed users enter together or none do, within the attraction's capacity — and adds leaving attractions and consulting their status so entry and exit results can be verified.

## What Changes

- `ATTRACTION ENTER` with a group ticket valid for the current date lets all users of its composition in at once; if capacity is insufficient for all (or any is already inside), nobody enters. Same mechanism as individual tickets.
- `ATTRACTION EXIT;AttractionIdentifier;Email`: removes that user from the attraction and frees the place; group-ticket users leave individually.
- `ATTRACTION STATUS;AttractionIdentifier`: shows identifier, name, maximum capacity, number of users inside, available places and the users inside, from the attraction's own state.
- HELP lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.
- Record the mandatory milestone commit `[F2-C07] Add group attraction access` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-management`: adds group ticket access (atomic), exit and status.
- `command-processing`: HELP lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## Impact

- `AttractionService.enter` (guard removed), `exit`; `CommandProcessor` EXIT/STATUS handlers and HELP.
