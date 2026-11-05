# Proposal

## Why

Milestone F2-C05 (Delivery 2, step 5) adds attractions to the park. Each has a unique identity, a name, a maximum capacity and the collection of users currently inside it. `ATTRACTION LIST` shows who is inside an attraction (source §6). Today the park has no attractions, and the later access milestones need them.

## What Changes

- Add attractions with a unique non-empty identifier, a non-empty name, a maximum capacity (a positive whole number) and a collection of the users currently inside. The relationship is one-way: the attraction references its occupants, and users never reference attractions.
- Add `ATTRACTION CREATE;Identifier;Name;Capacity`, rejecting duplicate identifiers and invalid names or capacities.
- Add `ATTRACTION LIST;AttractionIdentifier`: a read-only listing of the emails of the users currently inside, or a controlled "no users" result when empty. Unknown attractions are rejected.
- Attractions are held by the central park management object, in memory. Persistence is the "complete park persistence" milestone.
- List both commands in `HELP`.

In scope: the attraction model, creation and listing.

Out of scope: entering and leaving attractions and capacity enforcement on entry (next milestones), and persistence.

## Capabilities

### New Capabilities
- `attractions`: Park attractions — identity, name, capacity, current occupants — with creation and read-only listing of occupants.

### Modified Capabilities
- None.

## Impact

- New `model.Attraction`, `repository.AttractionRepository`, `command.AttractionCommands`; `ParkController` holds attractions; `CommandProcessor` dispatch and `HELP`; tests.
