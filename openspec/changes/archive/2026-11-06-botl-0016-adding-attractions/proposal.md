# Proposal

## Why

Milestone F2-C05 (Delivery 2, step 5) adds the park's attractions — each with an identity, a name, a maximum capacity and the collection of users currently inside — and the read-only `ATTRACTION LIST` command to see who is inside an attraction. Access to attractions (entering with tickets) builds on this in F2-C06/F2-C07.

## What Changes

- `ATTRACTION CREATE;Identifier;Name;Capacity`: unique identifier, non-empty name, positive whole-number capacity; the attraction starts empty.
- The attraction holds references to the users currently inside it; users hold no reference to attractions.
- `ATTRACTION LIST;AttractionIdentifier`: prints the email addresses of the users currently inside, or a controlled empty result; it never changes the attraction.
- Attractions are reachable through the central park management; `HELP` lists the new commands.

### In scope
- Source section 6.

### Out of scope
- Entering/leaving attractions with tickets (F2-C06, F2-C07), attraction persistence (F2-C08).

## Capabilities

### New Capabilities
- `attractions`: attraction creation, its identity/name/capacity/occupants, and listing its current occupants.

### Modified Capabilities
- `execution-control`: `HELP` lists `ATTRACTION CREATE` and `ATTRACTION LIST`.

## Impact

- New `Attraction`, `AttractionRepository`, `AttractionService`, `AttractionException`; `ParkController` exposes the attraction service; `CommandProcessor` dispatches the two commands.
