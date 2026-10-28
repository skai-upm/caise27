# Design

## Context

`Attraction` is a concrete class (identifier, name, capacity, occupants, all-or-nothing `admit(List<User>)`); `ParkController.enterAttraction` checks the ticket date, gathers users from `getIndividualTickets()` and calls `admit`. Passes are found by traversal in the controller.

## Decisions

- **Capabilities** (`business.model`): `Admission` (general access: `getIdentifier()`, `getDate()`, `getIndividualTickets()`), `IndividualAdmission extends Admission` (`getUser()`), `GroupAdmission extends Admission` (`getGroup()`). `Ticket` implements `Admission`; `IndividualTicket implements IndividualAdmission`; `GroupTicket implements GroupAdmission`.
- **`abstract class Attraction<T extends Admission>`** holding `Class<T> admissionType`; typed `enter(T admission, Collection<Pass> passes)` calls the typed hook `checkEntry(T, passes)` (default none) and `admit` of the admission's users; `enterWith(Admission, passes)` is the single runtime gate used by the controller: it rejects a ticket that is not an instance of `T` (`OperationNotAllowedException`) and otherwise casts and delegates. The type parameter therefore determines which tickets `enter` and `checkEntry` can receive.
- **Subclasses**: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAdmission>`, `GroupAttraction extends Attraction<GroupAdmission>`, `SpecialAttraction extends Attraction<IndividualAdmission>` overriding `checkEntry` to require an unconsumed `SpecialPass` whose ticket is the admission.
- **`AttractionType` enum** (GENERAL, INDIVIDUAL, GROUP, SPECIAL) with `create(id, name, capacity)` factory and `parse(text)` (`InvalidValueException`); each attraction reports `getType()`.
- **Controller**: `createAttraction(id, name, capacity[, type])`; `enterAttraction` passes the ticket and the passes of its individual tickets to `enterWith`.
- **Presentation**: `ControllerCommand` gains a (min, max) field-count constructor; `ATTRACTION CREATE` accepts 3 or 4 fields; `ATTRACTION STATUS` prints `TYPE:`.
- **Persistence**: `id;name;capacity;type`; 3-field lines → GENERAL.
- **UML**: interfaces, enum, generic `Attraction<T extends Admission>`, subclasses with bound realisations; `UmlModelTest` learns `interface`/`enum` declarations.
