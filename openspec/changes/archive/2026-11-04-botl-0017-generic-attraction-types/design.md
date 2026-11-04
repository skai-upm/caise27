# Design

## Context

`Attraction` is a concrete class whose `enter(Ticket, today)` uses `Ticket.getHolders()`. Tickets are resolved from text ids by `ParkController`, so the concrete kind is only known at runtime. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Generics that actually constrain operations, plus a single runtime check for text commands.

**Non-Goals:**
- Special Pass consumption rules and SpeedPass handling at entry (next change); a separate ticket hierarchy for SPECIAL.

## Decisions

- **Interfaces (business.model):**
  - `Admissible { String getId(); LocalDate getDate(); List<User> getHolders(); }`
  - `IndividualAccess extends Admissible { User getOwner(); }`
  - `GroupAccess extends Admissible { List<IndividualTicket> getConstituents(); }`
  - `Ticket implements Admissible`, `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`. No new methods are needed on the tickets: the existing ones satisfy the interfaces.
- **Generic attraction:** `abstract class Attraction<T extends Admissible>`:
  - It holds `Class<T> accepted` plus the existing id, name, capacity and present users.
  - `enter(T ticket, LocalDate today)` and `exit(T ticket)` are typed, and there is an abstract `type()`.
  - `IndividualAttraction` declares `Attraction<IndividualAccess>`, so calling `enter(groupTicket, ...)` on it does not compile, and vice versa for `GroupAttraction`. `SpecialAttraction` is also `Attraction<IndividualAccess>`, so the next change can use `getOwner()` and the individual ticket's passes without casts.
- **What generics cannot check:** commands arrive as text and resolve to an `Admissible` whose concrete kind is unknown at compile time. `Attraction.enterAny(Admissible, today)` / `exitAny(Admissible)` therefore check `accepted.isInstance(ticket)` (`ERROR: attraction <id> (<TYPE>) does not accept <kind> tickets`) and then `accepted.cast(...)` into the typed method. This is the only runtime switch, and it is driven by the class's type parameter, not by an enum `switch`.
- **`AttractionType`** enum with `create(id, name, capacity)` returning the matching subclass and `parse(String)` (case-insensitive, `Locale.ROOT`).
- **Commands:** `ATTRACTION CREATE` accepts 3 or 4 parameters; with 3 the type is `GENERAL`, so the E2 syntax is unchanged.
- **Persistence:** written as `id;name;capacity;TYPE`; read with 3 fields as `GENERAL` (legacy) or 4 fields with a known type; other lines are skipped.
- **Status:** a new line `  type: <TYPE>` after the header.
- **UML test:** `UmlModelTest` strips generic parameters (`Attraction<T extends Admissible>` → `Attraction`) when collecting declared names.

## Risks / Trade-offs

- [Existing `attractions.txt` lines are rewritten with a type on the next save] → Backward-compatible read; the new format is a superset.
- [STATUS output gains a line] → Required to make the type visible; other lines are unchanged.
