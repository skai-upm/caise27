# Design

## Context

`ParkController` centralizes park state; command classes register handlers. Occupants can't be added until access milestones exist. See proposal.md - Why.

## Decisions

- **`Attraction`**: encapsulated `id`, `name`, `capacity` (int > 0) and an insertion-ordered `LinkedHashSet<String>` of occupant e-mails, exposed as an unmodifiable view. Package-private `enter`/`leave` methods refuse to go over capacity, so later access milestones and tests can use them. A set keeps the same user from being inside twice.
- **Capacity parsing**: a digit-only character check followed by `Integer.parseInt` (no regex, per the Delivery 1 restriction), guarding against overflow.
- **Output**: `ATTRACTION CREATED`. The list prints `ATTRACTION <id> USERS:` followed by one e-mail per line, or `ATTRACTION <id> USERS: (none)`.
- **Storage**: a `LinkedHashMap<String, Attraction>` in `ParkController`, cleared by `reset()`.
