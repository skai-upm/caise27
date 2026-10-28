# Proposal

## Why

Milestone F2-C05: the park must contain attractions that users will later enter with their tickets. Each attraction needs an identity, a name, a maximum capacity and the collection of users currently inside, and staff must be able to see who is inside with `ATTRACTION LIST`.

## What Changes

- Add attractions with a unique identifier, a name, a maximum capacity (simultaneous people) and a collection of the users currently inside. The relationship is unidirectional: the attraction references its occupants; users do not reference attractions.
- Add `ATTRACTION CREATE;Identifier;Name;Capacity` (unique identifier, non-empty name, capacity a positive whole number).
- Add `ATTRACTION LIST;AttractionIdentifier`: read-only; prints the emails of the users inside, one per line, or a controlled "no users" line when empty.
- Attractions are owned by the park controller; `HELP` lists both commands.
- Commit with the mandatory message `[F2-C05] Add attractions`.

## Capabilities

### New Capabilities
- `attractions`: Attraction data (identity, name, capacity, occupants), creation and listing of current occupants.

### Modified Capabilities
- `execution-control`: `HELP` also lists `ATTRACTION CREATE` and `ATTRACTION LIST`.

## Impact

- New `model.Attraction`, `repository.AttractionRepository`, `service.AttractionService`; `ParkController.attractions()`; processor commands.
- Entering/leaving (`ATTRACTION ENTER`/`EXIT`) are F2-C06/F2-C07; until then attractions have no occupants through commands.
- Ambiguities flagged:
  - Capacity must be a whole number ≥ 1 written with digits only.
  - Responses: `ATTRACTION CREATED`; LIST prints one email per line in order of entry, or `NO USERS IN ATTRACTION <id>` when empty.
