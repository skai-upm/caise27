# Proposal

## Why

Milestone F2-C05 adds attractions to the park: identified, named elements with a maximum capacity and the collection of users currently inside. Ticket-based access (F2-C06/F2-C07) needs them, and their occupants must be consultable.

## What Changes

- `ATTRACTION CREATE;Identifier;Name;Capacity` creates an attraction with a unique identifier, a name, a positive integer capacity and no occupants.
- `ATTRACTION LIST;AttractionIdentifier` displays the emails of the users currently inside (a controlled "no users" result when empty), without changing state.
- The attraction references its occupants; users do not reference attractions.
- Attractions are managed by the central park manager.
- Out of scope: entering attractions (F2-C06/F2-C07), persistence (F2-C08).

## Capabilities

### New Capabilities
- `attractions`: Attractions, their capacity and occupants, creation and listing.

### Modified Capabilities
<!-- None -->

## Impact

- New `model.Attraction`, `repository.AttractionRepository`, `service.AttractionService`; `Park.attractions()`; new commands and HELP lines.
