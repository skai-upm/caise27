# Proposal

## Why

The park's attractions are the place where users, groups and tickets meet. Delivery 2 closes by letting visitors enter and leave attractions with individual or group tickets valid for the system date, enforcing capacity atomically, and querying the live status of each attraction.

## What Changes

- Attractions with identifier, name, maximum capacity and the collection of users currently present: `ATTRACTION CREATE;<id>;<name>;<capacity>` and `ATTRACTION DELETE;<id>` (only when empty).
- `ATTRACTION ENTER;<attractionId>;<ticketId>` admits all users of the ticket (its holder, or every member ticket of a group ticket) handled uniformly through the common `Ticket` abstraction; the ticket must be dated exactly on the system date; capacity can never be exceeded and group entry is all-or-nothing.
- `ATTRACTION EXIT;<attractionId>;<ticketId>` releases the users of that ticket who are inside (no date check).
- `ATTRACTION STATUS;<attractionId>` shows identifier, name, capacity, current occupancy, available places and the users present, without distinguishing how they entered.
- Attraction definitions persist; occupancy is not persisted (attractions start empty, as at the start of the day).
- `HELP` lists the new commands; multi-line responses are allowed for `ATTRACTION STATUS`; `uml/model.puml` includes attractions.

## Capabilities

### New Capabilities
- `attractions`: Attraction data and persistence, entry and exit with tickets, capacity and date rules, and the status query.

### Modified Capabilities
- `command-processing`: "Terminal command input" allows multi-line responses for `ATTRACTION STATUS`; "HELP command" adds a scenario listing the ATTRACTION commands.

## Impact

- New `Attraction`, `AttractionManager`, `AttractionRepository` (`data/attractions.txt`); `CommandProcessor` and `App` wiring; `uml/model.puml`.
- New tests; earlier behavior unchanged.
