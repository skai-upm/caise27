# Proposal

## Why

Milestone F2-C07 (Delivery 2, step 7) makes the same `ATTRACTION ENTER` operation accept group tickets atomically within the attraction's capacity. It also adds leaving attractions (`ATTRACTION EXIT`) and consulting attraction status (`ATTRACTION STATUS`), so that the results of entry and exit can be verified (source §10, §11, §12). Today a group ticket is explicitly rejected at entry, nobody can leave an attraction, and there is no status view.

## What Changes

- Remove the temporary group-ticket rejection in `ATTRACTION ENTER`. A group ticket admits all users of its composition through the same access path, all or nothing: if capacity is insufficient for everyone who needs to enter, nobody enters. The validity rules are unchanged (the ticket exists, its date equals the system date, and presence does not depend on later date changes).
- Add `ATTRACTION EXIT;AttractionIdentifier;Email`: removes that user from the attraction's occupants and frees their place. Group members leave individually by their own email. An unknown attraction, unknown email, or user not inside is rejected.
- Add `ATTRACTION STATUS;AttractionIdentifier`: shows the identifier, name, maximum capacity, number of users inside, available places, and the users inside. It is computed from the attraction's own state and does not depend on how users entered.
- List both new commands in `HELP`.

In scope: group access, exit and status.

Out of scope: persistence of occupancy ("complete park persistence" milestone).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: the temporary "group tickets not yet accepted" requirement is removed and replaced by atomic group access. Exit and status requirements and their `HELP` listing are added.

## Impact

- `AttractionCommands` (guard removed, `exit`, `status`), `CommandProcessor` dispatch and `HELP`, tests.
