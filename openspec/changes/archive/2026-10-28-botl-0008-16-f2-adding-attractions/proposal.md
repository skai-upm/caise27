# Proposal

## Why

Milestone F2-C05 adds attractions to the park: each has a unique identifier, a name, a maximum capacity and the collection of users currently inside it. `ATTRACTION CREATE` registers attractions and `ATTRACTION LIST` shows the e-mails of the users currently inside one. Attraction access (F2-C06/F2-C07) builds on this.

## What Changes

- Add the attraction concept (identifier, name, capacity, occupants) managed by the park controller; the attraction references its occupants, users do not reference attractions.
- Add `ATTRACTION CREATE;Identifier;Name;Capacity` with unique identifiers and a positive integer capacity.
- Add read-only `ATTRACTION LIST;AttractionIdentifier` printing occupant e-mails, with a controlled message when empty.
- Extend `HELP`.
- Commit with the exact message `[F2-C05] Add attractions`.

## Capabilities

### New Capabilities
- `attractions`: Attraction registration and consultation of current occupants.

### Modified Capabilities
- `execution-control`: HELP lists `ATTRACTION CREATE` and `ATTRACTION LIST`.

## Impact

- `Attraction` model class, controller registry, two commands, tests.
- **Flagged ambiguities:**
  - Capacity format is not specified beyond "maximum number of people"; it must be a positive integer written with digits only (0, negatives and non-numbers are rejected).
  - Entering/leaving attractions is F2-C06; the occupant collection enforces capacity and no duplicates as model invariants, but no command fills it yet.
  - Output texts are not specified: success prints `ATTRACTION CREATED`; an empty list prints `NO USERS IN ATTRACTION <id>`.
