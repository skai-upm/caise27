# Design

## Context

`ParkController.enterAttraction` already works on the common ticket abstraction and calls the all-or-nothing `Attraction.admit`. `Attraction` has `removeOccupant(User)`.

## Goals / Non-Goals

**Goals:** verified group access, ATTRACTION EXIT, ATTRACTION STATUS.

**Non-Goals:** persistence (F2-C08), pass consumption (F3).

## Decisions

- **No new access code path for group tickets**: `GroupTicket.getIndividualTickets()` returns the composition, so the existing operation admits all members atomically. Tests cover it.
- **`Attraction.findOccupant(email)`** and `ParkController.exitAttraction(attractionId, email)`: looks the user up in the attraction's own occupants (not the user registry), then `removeOccupant`.
- **`Attraction.getAvailablePlaces()`** = capacity − occupants; STATUS output:
  ```
  ATTRACTION: <id>
  NAME: <name>
  CAPACITY: <capacity>
  USERS INSIDE: <n>
  AVAILABLE PLACES: <capacity-n>
  OCCUPANTS: <email>, <email> | (none)
  ```
- **Commands**: `AttractionExitCommand` (`ATTRACTION EXITED`), `AttractionStatusCommand`.
