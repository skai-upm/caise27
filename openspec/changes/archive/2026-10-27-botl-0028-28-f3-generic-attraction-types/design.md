# Design

## Context

`Attraction` is a concrete class; `AttractionController.enter` works on `Ticket.holders()` for any ticket type.

## Goals / Non-Goals

**Goals:** capability interfaces, generic attraction hierarchy whose type parameter governs admission, type-based acceptance, persistence of the type.
**Non-Goals:** SPEED variant and pass consumption (F3-C08).

## Decisions

- **Capabilities** (business.model):
  - `AttractionAccess` — `getId()`, `isValidOn(LocalDate)`, `holders()`; implemented by `Ticket` (so every ticket offers general access).
  - `IndividualAccess extends AttractionAccess` — `getUser()`; implemented by `IndividualTicket`.
  - `GroupAccess extends AttractionAccess` — `getComposition()`; implemented by `GroupTicket`.
- **`abstract class Attraction<T extends AttractionAccess>`** keeps id/name/capacity/occupants and gains `Class<T> accessType`, `AttractionType type()`, `T accessFor(AttractionAccess)` (returns the typed access or throws `OperationNotAllowedException("Attraction <id> does not accept this ticket type")`) and `List<User> admit(T access, LocalDate today)` with the existing all-or-nothing checks (validity, already inside, capacity). Subclasses fix `T`: `GeneralAttraction` (`AttractionAccess`), `IndividualAttraction` (`IndividualAccess`), `GroupAttraction` (`GroupAccess`), `SpecialAttraction` (`IndividualAccess`). The parameter is effective: `admit` only compiles with that access type, and F3-C08 adds type-specific pass handling on it.
- **`AttractionType`** enum with `create(id, name, capacity)` factory and `fromText`.
- **Controller**: `enter` resolves the ticket, then a private generic helper `<T extends AttractionAccess> List<User> enterTyped(Attraction<T> a, Ticket t)` calls `a.admit(a.accessFor(t), today)`.
- **Persistence**: `id;name;capacity;type`; 3-field lines → GENERAL.
- **UML**: diagram updated with the new interfaces/classes so the consistency test passes (full revision in F3-C09); its parser ignores generic parameters in declarations.
