# Design

## Context

`ParkController` owns users, groups and tickets; commands are grouped in per-area classes (`GroupCommands`, `TicketCommands`, …).

## Goals / Non-Goals

**Goals:** attraction entity with occupant collection; CREATE and LIST.
**Non-Goals:** entering/leaving (F2-C06/07), persistence (F2-C08).

## Decisions

- **`model/Attraction`**: `final String id`, `String name`, `final int capacity`, `List<User> occupants`; queries `getOccupants()` (unmodifiable), `occupancy()`, `freePlaces()`, plus package-ready mutators `hasRoomFor(int)`, `enter(User)`, `exit(User)` used by the access milestones.
- **`repository/AttractionRepository`**: `LinkedHashMap<String, Attraction>`, like the other repositories; `ParkController.start` creates a fresh one.
- **`AttractionCommands`** with `create` and `list`. Capacity parsed by scanning digits (no regex), length ≤ 9 to avoid overflow, value ≥ 1.
- LIST output is one multi-line string; empty case uses an explicit message rather than an empty line so it is visible in file runs.
