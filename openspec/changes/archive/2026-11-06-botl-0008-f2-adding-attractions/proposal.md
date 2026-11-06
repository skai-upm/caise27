# Proposal

## Why

Milestone F2-C05 adds the park's attractions, which later milestones use for access control with individual and group tickets. An attraction has an identity, name, capacity and a collection of users currently inside it. Operators also need a read-only way to see who is inside. None of this exists yet.

## What Changes

- Add attractions, each with a unique identifier, a name, a maximum capacity (positive whole number of people), and the collection of users currently inside it, kept by the central park controller. The relationship is one-way: the attraction refers to its occupants, and users never refer to attractions.
- Add `ATTRACTION CREATE;Identifier;Name;Capacity`, which creates an attraction with no occupants. Duplicate identifiers, missing fields and non-positive or non-numeric capacities are rejected.
- Add `ATTRACTION LIST;AttractionIdentifier`, which displays the emails of the users currently inside, or a controlled empty result. It never changes the attraction.
- List both commands in HELP.

### In scope
- Attraction model, creation, occupant listing, HELP.

### Out of scope
- Entering or leaving attractions, ticket validation and capacity enforcement on entry (individual and group access milestones). Generic attraction types and speed passes (Delivery 3).

## Capabilities

### New Capabilities
- `attractions`: park attractions with identity, name, capacity and current occupants, and the commands to create and inspect them.

### Modified Capabilities
- None.

## Impact

- New `es.upm.park.model.Attraction`. `ParkController` stores attractions.
- `CommandProcessor`: ATTRACTION CREATE/LIST and HELP. New tests.
