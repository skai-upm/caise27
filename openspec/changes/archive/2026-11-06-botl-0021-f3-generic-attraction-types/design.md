# Design

## Context

`Attraction` is a concrete class (id, name, capacity, occupant emails). `ParkController.enterAttraction` validates any `Ticket` polymorphically through `admittedUsers()`/`isValidOn()`. Passes reference their `IndividualTicket`. The next milestone adds SpeedPass entry options and pass consumption, so the entry hook designed here must leave room for them.

## Goals / Non-Goals

**Goals:** capability interfaces, bounded generic attractions where the bound restricts entry, types persisted and backward-compatible creation.
**Non-Goals:** SpeedPass options or pass consumption at entry.

## Decisions

- **Capability interfaces** (`business.model`):
  - `AccessTicket` (`getId()`, `isValidOn(date)`, `admittedUsers()`), implemented by `Ticket`.
  - `IndividualAccess extends AccessTicket` (`getOwnerEmail()`), implemented by `IndividualTicket`.
  - `GroupAccess extends AccessTicket` (`getComposition()` as `List<? extends IndividualAccess>`), implemented by `GroupTicket`.
- **`abstract class Attraction<T extends AccessTicket>`** keeps id, name, capacity and occupants, plus a `Class<T> accepted` token, because generics are erased and tickets come from an untyped map. Its methods are:
  - `boolean canHandle(AccessTicket)`, which is `accepted.isInstance`.
  - `List<String> admit(AccessTicket ticket, LocalDate today, EntryRules rules)`. It narrows the ticket to `T` (rejecting otherwise), checks validity, calls the typed hook `checkEntry(T ticket, EntryRules rules)`, checks presence and capacity, and then enters all users (all or nothing).
  - `abstract AttractionType getType()`.
  Subclasses are `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, and `SpecialAttraction extends IndividualAttraction`, which overrides `checkEntry` to require an unconsumed Special Pass.
- **`EntryRules`**: a functional port given by the controller (`boolean hasUnconsumedSpecialPass(IndividualAccess)`), so the model does not reach into the controller. The next milestone extends it for SpeedPasses and consumption.
- **`AttractionType` enum** (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) with `create(id, name, capacity)` as the factory and `parse(text)` (throws `InvalidInputException`).
- **Persistence**: `id;name;capacity;TYPE`. A three-field line is loaded as GENERAL.
- **Commands**: `ATTRACTION CREATE` accepts 3 or 4 arguments. Status adds `TYPE: X`, and the catalog gets the typed syntax.
- Callers that held `Attraction` now use `Attraction<?>`.

## Risks / Trade-offs

- [Runtime class token alongside the generic parameter] → It is required because of erasure, and it is the same type as the bound, so the bound remains the source of truth (the constructor takes `Class<T>`).
