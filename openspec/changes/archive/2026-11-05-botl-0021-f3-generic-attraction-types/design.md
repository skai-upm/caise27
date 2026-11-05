# Design

## Context

`Attraction` is concrete. `ParkController.enterAttraction` checks existence, date, duplicates and capacity, then admits `ticket.holders()`. Passes are held by `ParkController`. See proposal.md - Why.

## Decisions

- **Capabilities**:
  - `GeneralAccess` declares `getId()`, `getDate()` and `holders()`, so every ticket can be used through it.
  - `IndividualAccess extends GeneralAccess` and `GroupAccess extends GeneralAccess` are the narrower capabilities.
  - `IndividualTicket implements IndividualAccess` and `GroupTicket implements GroupAccess`.
- **Bounded generics with real effect**:
  - `Attraction<T extends GeneralAccess>` holds `Class<T> accessType`.
  - `accepts(GeneralAccess)` is `accessType.isInstance`.
  - `admissionFor(GeneralAccess, PassLookup)` casts to `T` and calls `protected void checkAdmission(T ticket, PassLookup passes)`. That method is typed by `T`, so `SpecialAttraction` receives an `IndividualAccess` and can look up its passes.
  - `ParkController` performs one generic entry flow for every type.
- **`SpecialAttraction extends Attraction<IndividualAccess>`** overrides `checkAdmission` to require an unconsumed `SpecialPass` on that ticket. It doesn't introduce a different ticket hierarchy.
- **`PassLookup`** is a functional interface implemented by `ParkController::passesOfTicket`. Attractions query passes without holding them.
- **`AttractionType` enum** has a `create(id, name, capacity)` factory and a `typeOf` accessor via an abstract `getType()` in `Attraction`.
- **Persistence** writes `id;name;capacity;type`. A 3-field line is GENERAL (compatibility).
- **Messages**: `Attraction <id> (<TYPE>) does not accept <individual|group> tickets`, `Ticket <id> has no unconsumed Special Pass`. Status adds `  TYPE: <TYPE>`.
