# Design

## Context

`Attraction` is a concrete class (id, name, capacity, present users). `AttractionManager.enter` finds a `Ticket`, checks date/inside/capacity and admits `ticket.getVisitors()`. `Ticket` is abstract with `IndividualTicket`/`GroupTicket`. Attractions persist as `<id>;<name>;<capacity>`.

## Goals / Non-Goals

**Goals:**
- Interfaces for access capabilities; bounded generics that do real work (type check and typed admission).

**Non-Goals:**
- Pass consumption and the `SPEED` option (next proposal); SPECIAL behaves like INDIVIDUAL for now regarding admission.

## Decisions

- **Capability interfaces** (`business.model`): `Admission` (`getId()`, `getDate()`, `getVisitors()`), `IndividualAdmission extends Admission` (`getHolder()`), `GroupAdmission extends Admission` (`getTickets()`). `Ticket implements Admission`, `IndividualTicket implements IndividualAdmission`, `GroupTicket implements GroupAdmission`.
- **Generic attraction**: `abstract class Attraction<T extends Admission>` holds a `Class<T> admitted` token; `accepts(Admission)` = `admitted.isInstance`, `admit(T admission)` adds `admission.getVisitors()`, `canAdmit(T)`, `release(...)`. Subclasses fix the bound: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAdmission>`, `GroupAttraction extends Attraction<GroupAdmission>`, `SpecialAttraction extends Attraction<IndividualAdmission>`; each returns its `AttractionType`.
- **Typed entry** in `AttractionManager`: a generic helper `<T extends Admission> String enter(Attraction<T> attraction, Admission ticket)` checks `accepts`, converts with `attraction.cast(ticket)` (`admitted.cast`) and continues with typed `T` for capacity and admission, so an attraction can only ever receive its bound type.
- **`AttractionType` enum** with `create(id, name, capacity)` factory and `parse(text)` (ignore case, null if unknown).
- **Order of checks**: attraction → ticket → kind accepted → date → users inside → capacity.
- **Status**: new line `Type: <TYPE>` after the header.
- **Persistence**: 4th field = type; 3-field lines → GENERAL (backward compatible).
- **UML/UmlModelTest**: generic declarations `abstract class Attraction<T extends Admission>`; interface-to-interface extension drawn with `--|>`; the test parser accepts generic class headers, treats extended interfaces as inheritance, and resolves parameterised element types (`Attraction<?>`).

## Risks / Trade-offs

- [Unchecked cast warnings] → Avoided by using `Class.cast` with the token.
