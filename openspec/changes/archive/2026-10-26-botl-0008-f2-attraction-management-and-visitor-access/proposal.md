# Proposal

## Why

Users, groups and tickets exist, but nothing uses the tickets yet. The park needs attractions that visitors enter with their tickets — individually or as a group — respecting the ticket date and the attraction's capacity, and operators need to see each attraction's live occupancy.

## What Changes

- New command `ATTRACTION CREATE;<id>;<name>;<capacity>` creating an attraction with a unique identifier, a name, a positive maximum capacity and an (initially empty) collection of users present.
- New command `ATTRACTION ENTER;<attractionId>;<ticketId>` admitting the users covered by an individual ticket (its user) or a group ticket (the users of its internal tickets), handled uniformly through the common ticket abstraction. The ticket must be dated on the current system date; capacity can never be exceeded; group entry is atomic (all members enter or none).
- New command `ATTRACTION EXIT;<attractionId>;<ticketId>` making the ticket's users leave and freeing their places (no date check on exit).
- New command `ATTRACTION STATUS;<attractionId>` showing identifier, name, maximum capacity, current occupancy, available places and the users present, without distinguishing how they entered.
- Attractions (identifier, name, capacity) are persisted; the users present are not — every execution starts with empty attractions.
- HELP lists the new commands; the PlantUML model is updated to stay consistent with the implementation.

## Capabilities

### New Capabilities
- `attraction-management`: Creating attractions, entering and leaving them with individual or group tickets, capacity and date rules, atomic group entry, status queries, and attraction persistence.

### Modified Capabilities
<!-- None -->

## Impact

- New `Attraction`, `AttractionRepository`, `AttractionManager`; `CommandProcessor` gains four ATTRACTION commands; `App` wires `data/attractions.txt`.
- `uml/model.puml` updated. Tests for creation, entry rules, exit, status and persistence. No new dependencies.
