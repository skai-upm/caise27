# Proposal

## Why

Milestone F2-C08 requires that the whole park state — users, groups and their members, individual tickets, group tickets with their composition and group reference, and attractions — survive between executions, including historical tickets of deleted users and groups. It also adds the `--fly` mode for isolated, repeatable runs that leave no data behind.

## What Changes

- Persist groups (with members), individual tickets (with their user), group tickets (composition and group reference while the group exists) and attractions in text files next to the users file; reconstruct them at startup with object references.
- Historical tickets keep the information needed to rebuild them even when their user or group no longer exists.
- Attraction occupants are not persisted: every execution starts with all attractions empty.
- `--fly` as first argument (optionally followed by a command file): all repositories use files inside a new timestamped directory `tmp/<yyyyMMdd_HHmmss>/`, deleted with its files when the execution ends via EXIT or at the end of the input. Without `--fly`, argument handling is unchanged.
- No Java object serialization, databases, frameworks or external libraries.
- Record the mandatory milestone commit `[F2-C08] Extend park persistence` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `persistence`: adds persistence of groups, tickets and attractions, historical ticket preservation, empty attractions at startup, and the `--fly` temporary mode.
- `command-processing`: adds the `--fly` execution forms.

## Impact

- New `GroupFileStorage`, `TicketFileStorage`, `AttractionFileStorage`; repositories accept a storage; `ParkController.initialize` takes all repositories and offers `persistAll()`; `CommandProcessor` persists after each command; `App` handles `--fly` and builds the repositories from a data directory.
