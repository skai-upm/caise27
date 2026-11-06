# Proposal

## Why

Milestone F2-C07 requires the attraction access operation to support group tickets atomically within the attraction's capacity, and requires attraction status to be consultable so that entries and exits can be verified. Entry is currently specified only for individual tickets. Users cannot leave an attraction, and there is no status view with capacity and free places.

## What Changes

- Specify and verify entry with a group ticket through the existing access operation. All users of the group ticket's composition enter together, and only if the ticket is valid today, none of them is already inside, and capacity suffices for all. Otherwise nobody enters.
- Add `ATTRACTION EXIT;AttractionIdentifier;Email`, which removes that user from the occupants and frees the place. Members who entered with a group ticket leave one by one with their own email.
- Add `ATTRACTION STATUS;AttractionIdentifier`, which shows the identifier, name, maximum capacity, number of users inside, available places and the users inside. All of it is read from the attraction's own state, independently of how users entered.
- List both new commands in HELP.

### In scope
- Group entry rules, exit, status, HELP.

### Out of scope
- Persistence of occupants (complete park persistence milestone). Delivery 3 attraction types and pass rules.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: adds group-ticket entry, leaving an attraction, attraction status and their HELP entries.

## Impact

- `ParkController.exitAttraction`. `CommandProcessor` gets the ATTRACTION EXIT / STATUS handlers and HELP lines.
- Tests for group entry atomicity, exit and status. No model change: `Attraction` already provides `leave` and its occupant view, and `GroupTicket` already lists the users it admits.
