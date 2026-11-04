# Design

## Context

`Attraction` (model) is concrete: `enter(Ticket, today)` relies on the polymorphic `Ticket.getVisitors()`; `exit(Ticket)`. `Ticket` is abstract with `IndividualTicket` and `GroupTicket`. `AttractionService.create(id, name, capacityText)`, `AttractionRepository` (`Id;Nombre;Aforo`). `ParkController.enterAttraction` resolves `Ticket` and calls `enter`.

## Goals / Non-Goals

**Goals:** types expressed by interfaces + bounded generics that actually decide admission; backward-compatible syntax and data.

**Non-Goals:** Special Pass consumption/conditions (next change), new ticket hierarchy for SPECIAL.

## Decisions

- **Capabilities**: `interface Admission { String getId(); LocalDate getDate(); List<User> getVisitors(); }`; `interface IndividualAccess extends Admission { User getUser(); }`; `interface GroupAccess extends Admission { List<IndividualTicket> getConstituents(); }`. `Ticket implements Admission`; `IndividualTicket implements IndividualAccess`; `GroupTicket implements GroupAccess`.
- **Generic attraction**: `abstract class Attraction<T extends Admission>` with `private final Class<T> acceptedType` passed by each subclass. `admit(Admission ticket, LocalDate today)` checks `acceptedType.isInstance(ticket)` (else `IllegalArgumentException` "does not admit …"), then calls `enter(acceptedType.cast(ticket), today)`. `enter(T, today)` performs the shared date/capacity/presence checks and calls the protected hook `checkAdditionalConditions(T ticket, LocalDate today)` (no-op by default) before admitting all visitors atomically. `exit(Admission)` unchanged in behaviour.
- **Subclasses**: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides the hook in the next change). `getType()` returns the `AttractionType`.
- **Factory**: `enum AttractionType { GENERAL, INDIVIDUAL, GROUP, SPECIAL; Attraction<?> create(id, name, capacity); static AttractionType parse(String) }`.
- **Persistence**: write `Id;Nombre;Aforo;Tipo`; read 3 or 4 fields (3 → GENERAL).
- **Presentation**: `ATTRACTION CREATE` accepts 3 or 4 parameters; STATUS adds `  Type: <TYPE>` after the title line.
- **UML**: interfaces, realizations, interface extension, generic class with its subclasses.

## Risks / Trade-offs

- [Wildcard types `Attraction<?>` in services] → admission goes through `admit(Admission)`, which is type-safe thanks to `Class.cast`; no unchecked casts.
