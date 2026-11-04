# Design

## Context

`Attraction` is a concrete class whose `enter(Ticket, LocalDate)` / `exit(Ticket)` use only `Ticket.getDate()` and the polymorphic `Ticket.getVisitors()`. Tickets are resolved at run time from identifiers (`TicketManager.findAnyTicket`). Attractions persist as `Id;Nombre;Capacidad`.

## Goals / Non-Goals

**Goals:** a type hierarchy where the accepted ticket kind is a generic bound that the compiler enforces, plus an explicit, single run-time check for identifiers typed by users.

**Non-Goals:** Special Pass and SpeedPass consumption rules (pass-based access change); a separate ticket hierarchy for SPECIAL.

## Decisions

- **Capability interfaces (`business.model`).**
  - `AttractionAccess { String getId(); LocalDate getDate(); List<User> getVisitors(); }` — implemented by the abstract `Ticket` (which already has these methods).
  - `IndividualAccess extends AttractionAccess { Optional<User> getUser(); }` — implemented by `IndividualTicket` (one holder).
  - `GroupAccess extends AttractionAccess { List<IndividualTicket> getParts(); }` — implemented by `GroupTicket`.
  These carry behaviour that differs per capability (single holder vs constituent tickets), so the bounds are not decorative.
- **Generic attraction.** `abstract class Attraction<T extends AttractionAccess>` keeps id, name, capacity and present users, and exposes:
  - `enter(T access, LocalDate today)` and the unchanged atomic rules (date equals today, visitors exist, none already present, capacity);
  - `abstract Class<T> accessType()` and `abstract AttractionType getType()`;
  - `admit(AttractionAccess access, LocalDate today)`: `if (!accessType().isInstance(access)) → UserException("Attraction X (TYPE) does not admit <kind> tickets")`, else `enter(accessType().cast(access), today)`;
  - `exit(AttractionAccess)` unchanged and unrestricted (a visitor inside can always leave).
  Subclasses: `GeneralAttraction extends Attraction<AttractionAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (a distinct class so that the pass-based access change can override its entry rules).
- **What the compiler prevents vs what is checked at run time.**
  - Compile time: code holding a `GroupAttraction` cannot call `enter` with an `IndividualTicket` (or an `IndividualAccess`), and an `IndividualAttraction`/`SpecialAttraction` cannot be given a `GroupTicket`; a `GeneralAttraction` accepts any `AttractionAccess`. A test documents this with a compile-time-typed usage, and a commented negative example in the design is not needed in code.
  - Run time: the controller receives an attraction from a map (`Attraction<?>`) and a ticket by identifier (`Ticket`), whose static types are unknown, so `admit` checks `accessType().isInstance(...)` exactly once before the typed `enter`. This is the only run-time type check.
- **`AttractionType` enum** `GENERAL, INDIVIDUAL, GROUP, SPECIAL` with `create(id, name, capacity)` returning the matching subclass, and `parse(text)` (case-insensitive, `UserException` for unknown values).
- **Creation.** `CommandProcessor` accepts 3 or 4 parameters for `ATTRACTION CREATE`; `ParkController.createAttraction(id, name, capacity, typeText)` passes `null` type for the 3-parameter form → GENERAL. Error message for wrong counts keeps the existing format.
- **Persistence.** Lines `Id;Nombre;Capacidad;Tipo`; on load 3 fields → GENERAL, 4 fields → parsed type, unknown type → line skipped with a warning.
- **Status.** First line becomes `OK: Attraction A01 (Tornado) capacity=20 present=0 available=20 type=GENERAL` — the previous prefix is unchanged.
- **Help.** A second catalog entry `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` right after the existing one.
- **UML.** Interfaces with realisations, abstract generic `Attraction` (`Attraction<T>` noted in the class name as `Attraction` with stereotype-free generic declaration), four subclasses, `AttractionType` enum, `AttractionManager *--> Attraction : attractions`, `Attraction o--> User : present`.

## Risks / Trade-offs

- [Run-time cast remains for user-supplied identifiers] → unavoidable when types come from text; confined to `admit`.
- [Changing `Attraction` to abstract touches tests] → tests switch to `GeneralAttraction` where the old behaviour is expected.
