# Proposal

## Why

Milestone F2-C05 adds the park's attractions: each has an identity, a name, a maximum capacity and the collection of users currently inside, and `ATTRACTION LIST` shows who is inside. Access (enter/exit) builds on this in F2-C06/F2-C07.

## What Changes

- `ATTRACTION CREATE;Identifier;Name;Capacity`: unique identifier, name, maximum simultaneous capacity (positive integer); starts empty.
- Attractions hold references to the users inside them; users hold no reference to attractions.
- `ATTRACTION LIST;AttractionIdentifier`: read-only listing of the emails of the users currently inside, with a controlled message when empty.
- HELP lists `ATTRACTION CREATE` and `ATTRACTION LIST`.
- Record the mandatory milestone commit `[F2-C05] Add attractions` once verified.

## Capabilities

### New Capabilities
- `attraction-management`: Park attractions — creation, capacity, occupants, access and status queries.

### Modified Capabilities
- `command-processing`: HELP lists the ATTRACTION CREATE and ATTRACTION LIST commands.

## Impact

- New `Attraction`, `AttractionRepository`, `AttractionService`, `AttractionOperationException`; `ParkController` gives access to attractions; `CommandProcessor` dispatch and HELP.
