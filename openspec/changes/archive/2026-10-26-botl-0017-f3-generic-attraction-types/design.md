# Design

## Context

`Attraction` is a concrete class whose `admit(Ticket)`/`release(Ticket)` use `Ticket.getUsers()`; `AttractionController` stores `List<Attraction>`; `AttractionRepository` writes `id;name;capacity`. `CommandProcessor`'s registry maps one operation to one syntax and checks the exact parameter count.

## Goals / Non-Goals

**Goals:**
- Capability interfaces + bounded generics that do real work: the bound decides which tickets are admitted (runtime check through the stored `Class<T>`) and the subclass receives a typed ticket.
- A hook for SPECIAL's pass rules (next change) without touching the other types.

**Non-Goals:**
- Pass consumption at entry (F3-C08).

## Decisions

- **Capability interfaces** (model): `Admission` (`getId()`, `getDate()`, `getUsers()`) — any ticket that grants access; `IndividualAdmission extends Admission` (`getUser()`) — access of one user; `GroupAdmission extends Admission` (`getTickets()` → `List<IndividualTicket>`) — access of a group through its members' tickets. `Ticket implements Admission`, `IndividualTicket implements IndividualAdmission`, `GroupTicket implements GroupAdmission`.
- **Generic attraction**: `abstract class Attraction<T extends Admission>` keeps id, name, capacity, visitors and a `Class<T> admitted`. `admit(Admission ticket)`: if `!admitted.isInstance(ticket)` → `ParkException("Attraction A02 (GROUP) does not admit individual tickets")`; otherwise `T typed = admitted.cast(ticket)`, `checkAccess(typed)` (hook, no-op by default), then the existing all-or-nothing capacity logic on `usersOf(typed)`. `release(Admission)` is unchanged (exit has no type rule).
- **Types**: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAdmission>`, `GroupAttraction extends Attraction<GroupAdmission>` (its `usersOf` maps the member tickets to their users), `SpecialAttraction extends Attraction<IndividualAdmission>` (overrides `checkAccess` in F3-C08). `getType()` returns the `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`), whose `create(id, name, capacity)` instantiates the matching subclass and `parse(text)` validates user input.
- **Controller**: `create(id, name, capacityText, typeText)` (type defaults to GENERAL when the 4-parameter form is used); collection type `List<Attraction<?>>`.
- **Persistence**: `id;name;capacity;TYPE`; 3-field lines load as GENERAL.
- **Commands**: the registry gains a second `ATTRACTION CREATE` syntax with `Tipo`; lookup chooses the registered syntax of that operation whose parameter count matches, otherwise reports the usage of the first one. Status prints `Type: <TYPE>` after `Name:`.
- **UML**: the three interfaces with their realisations by the ticket classes; `Attraction<T extends Admission>` abstract with four subclasses; `AttractionType` enum; associations unchanged (`visitors`).

## Risks / Trade-offs

- [`Class<T>` token duplicates the type argument] → Needed because Java erases generics at runtime; each subclass passes its own token in the constructor, so they cannot diverge.
