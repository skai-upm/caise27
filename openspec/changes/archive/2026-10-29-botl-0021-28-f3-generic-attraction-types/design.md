# Design

## Context

`Attraction` is a concrete class (capacity, occupants); `ParkController.enterAttraction` checks date, duplicates and capacity using `Ticket.admittedUsers()`. Passes are in the park's pass registry; tickets hold no pass references.

## Goals / Non-Goals

**Goals:** types, generic restriction, typed creation and persistence. **Non-Goals:** SpeedPass entry option and pass consumption (F3-C08).

## Decisions

- **Capability interfaces** (`business.model`): `AccessTicket` (`getIdentifier()`, `getDate()`, `admittedUsers()`), `IndividualAccess extends AccessTicket` (`getUser()`), `GroupAccess extends AccessTicket` (`getComposition()`). `Ticket implements AccessTicket`; `IndividualTicket implements IndividualAccess`; `GroupTicket implements GroupAccess`.
- **`abstract class Attraction<T extends AccessTicket>`**: keeps identity, capacity and occupants; constructor takes `Class<T> accessType`; `getType()` abstract (`AttractionType`); `T accessFor(Ticket)` → cast or `OperationNotAllowedException("attraction X of type T does not accept ...")`; `List<User> enter(T ticket, LocalDate today, PassLookup passes)` is a template method: date check, `checkAdmission(ticket, passes)` hook, duplicates, capacity, then all occupants added. Subclasses: `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding `checkAdmission` to require `passes.hasUnconsumedSpecialPass(ticket)`.
- **`PassLookup`** (functional interface in `business.model`): lets the special attraction ask for pass state without tickets referencing passes; `ParkController` provides it.
- **`AttractionType`** enum with `create(id, name, capacity)` factory and `parse(text)`.
- **Controller**: `createAttraction(id, name, capacityText[, typeText])`; `enterAttraction` delegates to a generic helper `<T extends AccessTicket> List<User> enter(Attraction<T>, Ticket)`; collections use `Attraction<?>`.
- **Persistence**: `id;name;capacity;type`; 3-field lines → GENERAL.
- **Presentation**: ATTRACTION CREATE accepts 3 or 4 arguments; STATUS prints `TYPE: <type>`.

## Risks / Trade-offs

- [Widespread `Attraction` → `Attraction<?>` change] → compile-checked; tests updated to concrete subclasses.
