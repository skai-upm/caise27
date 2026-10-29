# Proposal

## Why

Milestone F2-C07 extends the same `ATTRACTION ENTER` operation to group tickets — all users of the composition enter together, atomically, within the attraction's capacity — and adds the means to verify entries and exits: `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## What Changes

- `ATTRACTION ENTER;<attraction>;<groupTicket>`: all users of the group ticket's composition enter together through the same common-abstraction mechanism; if the attraction lacks room for all of them (or any of them is already inside), none enters.
- `ATTRACTION EXIT;AttractionIdentifier;Email`: removes that user from the attraction's occupants and releases the place; users who entered with a group ticket leave individually.
- `ATTRACTION STATUS;AttractionIdentifier`: displays identifier, name, maximum capacity, number of users inside, available places and the users inside, computed from the attraction's own state, regardless of how users entered.
- HELP lists `ATTRACTION EXIT` and `ATTRACTION STATUS`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: group-ticket entry (atomic), attraction exit and attraction status; the common-access requirement now covers group tickets.
- `command-interface`: HELP lists ATTRACTION EXIT and ATTRACTION STATUS.

## Impact

- `ParkController.enterAttraction` (guard removed), new `exitAttraction`; `CommandProcessor` STATUS/EXIT output; tests.

## Ambiguities and unmet prerequisites (flagged)

- Users are still registered only programmatically (no USER CREATE in this batch yet), so group tickets can only be built for programmatically registered users; the behavior is verified by tests.
- ATTRACTION EXIT for a user who is not inside (or an unknown attraction) is rejected with an error; the source does not state this explicitly but it is input that cannot be processed.
- The STATUS layout is free per the source; one labelled line per item is used, with the users inside listed by email (`none` when empty). EXIT confirmation: `USER <email> LEFT ATTRACTION <id>`.
