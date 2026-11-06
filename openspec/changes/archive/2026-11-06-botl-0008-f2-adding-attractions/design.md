# Design

## Context

`ParkController` centralizes groups and tickets. Users are referenced by email throughout, because the user model does not exist yet (it comes from later milestones in this batch).

## Goals / Non-Goals

**Goals:** an attraction model ready for the access milestones (occupants and capacity), and a read-only listing.
**Non-Goals:** entry/exit operations and access rules.

## Decisions

- **`Attraction`** (`es.upm.park.model`): final `id`, `name`, `capacity` (int > 0) and a `LinkedHashSet<String>` of occupant emails. The set keeps entry order for listing and prevents duplicates. It exposes an unmodifiable occupant view, plus public `enter`/`leave` primitives for the access milestones. The model enforces only the structural invariants (capacity > 0, no duplicates). Access rules belong to the controller in later milestones.
- **Occupants referenced by email**, consistent with groups and tickets until the user model exists. Users hold no reference to attractions.
- **Capacity parsing** uses `Integer.parseInt` on the trimmed text, so non-numeric, decimal and out-of-range values are rejected. Positivity is checked in the controller.
- **LIST output**: one email per line under `USERS IN <id>:`, or `NO USERS IN <id>` when empty. It is built from the read-only view, so listing cannot change state.

## Risks / Trade-offs

- [Capacity is not enforced yet] → Nothing can enter an attraction until the access milestone, which adds enforcement.
