# Design

## Context

`ParkController` owns users, groups, tickets, date and price. See proposal.md.

## Goals / Non-Goals

**Goals:** attraction model ready for entry/exit; read-only listing.
**Non-Goals:** `ATTRACTION ENTER/EXIT/STATUS` (F2-C06/F2-C07), persistence.

## Decisions

- **`model.Attraction`**: final `id`, `name`, final `capacity`, `List<User> occupants` (insertion order = entry order) with `getOccupants()` (copy), `isFull()`, `contains(User)`, `addOccupant(User)`, `removeOccupant(User)`; references users directly (in-place `USER UPDATE` keeps them current).
- **`repository.AttractionRepository`** (ordered map) and **`service.AttractionService`** (`createAttraction`, `findById`, `listOccupantEmails`).
- **Capacity parsing** in the processor: digits only, then `Integer.parseInt` guarded for overflow, ≥ 1.

## Risks / Trade-offs

- [Occupant operations exist before commands use them] → minimal, needed for listing tests; commands arrive next milestone.
