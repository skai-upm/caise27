# Proposal

## Why

Milestone F2-C05 adds the park's attractions — identity, name, maximum capacity and the users currently inside — together with `ATTRACTION LIST` to see who is inside, the base for the access milestones that follow.

## What Changes

- Add attractions with a unique identifier, name, maximum capacity and a collection of the users currently inside (attraction → users only; users never reference attractions).
- `ATTRACTION CREATE;Identifier;Name;Capacity` creates an attraction (capacity: positive whole number).
- `ATTRACTION LIST;AttractionIdentifier` displays the emails of the users inside, or a controlled "no users" result; read-only.
- The central controller owns the attractions.
- Commit with the exact message `[F2-C05] Add attractions`.

Entering/leaving and status (`ATTRACTION ENTER/EXIT/STATUS`) are later milestones (F2-C06, F2-C07); until then attractions are always empty.

## Capabilities

### New Capabilities
- `attractions`: Attractions, their occupants, creation and the occupant listing.

### Modified Capabilities
<!-- none -->

## Impact

- New `model/Attraction`, `repository/AttractionRepository`, `command/AttractionCommands`; `ParkController` gains attractions; HELP lists the commands.
- Ambiguities flagged: capacity must be written as digits only, be at least 1 and at most 999999999; identifiers compare exactly; responses: `ATTRACTION CREATED`; LIST prints `Users in <id>:` followed by one email per line, or `No users in <id>` when empty.
