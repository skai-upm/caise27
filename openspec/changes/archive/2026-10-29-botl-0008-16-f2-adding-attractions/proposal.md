# Proposal

## Why

Milestone F2-C05 adds attractions to the park: each has a unique identifier, a name, a maximum capacity and the collection of users currently inside it, and `ATTRACTION LIST` lets the users currently inside an attraction be consulted.

## What Changes

- `ATTRACTION CREATE;Identifier;Name;Capacity` creates an attraction kept by central park management.
- Attractions hold references to the users inside them (unidirectional: users hold no reference to the attraction), never more than their capacity.
- `ATTRACTION LIST;AttractionIdentifier` displays the email addresses of the users currently inside, or a controlled "empty" result; it does not change state.
- HELP lists both commands.

## Capabilities

### New Capabilities
- `attractions`: attraction data, creation, occupant collection and occupant listing.

### Modified Capabilities
- `command-interface`: HELP lists the ATTRACTION CREATE and ATTRACTION LIST commands.

## Impact

- New `Attraction` class; attraction registry in `ParkController`; dispatch in `CommandProcessor`; tests.

## Ambiguities and unmet prerequisites (flagged)

- Entering and leaving attractions (ATTRACTION ENTER / EXIT, ticket checks) belong to later milestones. Until then no command can put users inside an attraction, so `ATTRACTION LIST` currently always yields the empty result through the command interface; the non-empty listing is exercised by tests that add occupants programmatically.
- Capacity validation is not specified beyond "maximum number of people"; a whole number greater than zero is required (digits only, no regular expressions). Blank identifier or name and duplicate identifiers are rejected.
- Output texts are not specified: `ATTRACTION CREATED`; LIST prints one email per line, or `NO USERS IN ATTRACTION <id>` when empty.
