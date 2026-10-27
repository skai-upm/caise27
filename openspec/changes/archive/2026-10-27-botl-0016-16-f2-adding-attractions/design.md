# Design

## Context

`ParkController` centralises users, groups, tickets, date and price. Delivery 2 direction rule: attractions know the users inside; users do not know attractions.

## Goals / Non-Goals

**Goals:**
- Attraction model and repository, creation and listing.

**Non-Goals:**
- Entering/leaving and status (F2-C06, F2-C07); attraction persistence (F2-C08).

## Decisions

- **`Attraction`**: `final String identifier`, `String name`, `final int capacity`, `List<User> occupants` (insertion order). Methods `getOccupants()` (unmodifiable), `getAvailablePlaces()`, and package-level `enter(List<User>)`/`exit(User)` to be used by the access milestones.
- **`AttractionRepository`** (LinkedHashMap by identifier) registered in `ParkController.initialize`.
- **`AttractionService`**: create (non-empty id/name, unique id, capacity parsed by the command layer) and `findAttraction`.
- **Capacity text (ambiguity, flagged):** only digit strings are accepted and the value must be ≥ 1; the source only says it is a maximum number of people, so 0 or negative capacities are rejected as meaningless.
- **LIST output:** one email per line; when empty, `No users inside attraction A01.`.
- **Output:** `ATTRACTION CREATED`; errors `ERROR: ...`.
- **Mandatory commit:** `[F2-C05] Add attractions` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Integer overflow on huge capacities] → Parsing errors are reported as invalid capacity.
