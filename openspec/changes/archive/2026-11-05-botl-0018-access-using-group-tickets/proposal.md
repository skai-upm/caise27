# Proposal

## Why

Milestone F2-C07 extends attraction access to group tickets — all members enter together or none do, within capacity — and adds the operations to leave an attraction (`ATTRACTION EXIT`) and to verify the results (`ATTRACTION STATUS`).

## What Changes

- `ATTRACTION ENTER` with a group ticket admits every user of its composition atomically; if capacity is insufficient for all of them (or any of them is already inside) nobody enters. Same entry path as individual tickets (common abstraction).
- `ATTRACTION EXIT;AttractionIdentifier;Email` removes the user from the attraction and frees the place; group members leave individually.
- `ATTRACTION STATUS;AttractionIdentifier` shows identifier, name, capacity, number of occupants, available places and occupants, from the attraction's own state, regardless of how users entered.
- Out of scope: persistence (F2-C08).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attraction-access`: adds group ticket entry, leaving attractions and attraction status.

## Impact

- `AttractionService.exit`/`status`; new commands and HELP lines; tests. Entry code unchanged (already type-independent).
