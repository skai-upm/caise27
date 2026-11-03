# Design

## Context

`Attraction` is a concrete class with `enter(ParkTicket, LocalDate)`; tickets share the abstract `ParkTicket` (`Ticket`, `GroupTicket`) with polymorphic `getVisitors()`. Attraction definitions are stored as `Id;Nombre;Capacidad`. See proposal.md.

## Goals / Non-Goals

**Goals:** four attraction types, access capabilities as interfaces, generics that genuinely restrict entry, backward-compatible syntax and files.

**Non-Goals:** SpeedPass/Special Pass consumption (F3-C08); a separate ticket hierarchy for SPECIAL.

## Decisions

- **Capability interfaces (business.model):** `AccessTicket { getId(); getDate(); getVisitors(); }`; `IndividualAccess extends AccessTicket` and `GroupAccess extends AccessTicket`. `ParkTicket implements AccessTicket`; `Ticket implements IndividualAccess`; `GroupTicket implements GroupAccess`. SPECIAL reuses `IndividualAccess` — no extra ticket class.
- **Generic attraction:** `abstract class Attraction<T extends AccessTicket>` keeps id, name, capacity, present users and a `Class<T> admitted` token. `admit(T ticket, LocalDate today)` contains the existing atomic entry logic (date, already present, capacity). Subclasses fix `T`: `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (hook for F3-C08 rules).
- **What the bound prevents at compile time:** any code holding an `IndividualAttraction`/`SpecialAttraction` cannot pass a `GroupTicket` to `admit`, and a `GroupAttraction` cannot receive a `Ticket` (both are compile errors); `GeneralAttraction` accepts both. **What is validated at run time:** command input resolves a ticket by id to a `ParkTicket` whose concrete kind is unknown statically; `enter(AccessTicket, LocalDate)` checks `admitted.isInstance(ticket)` and then calls `admit(admitted.cast(ticket), today)`, otherwise rejects with `Attraction <id> (<TYPE>) does not admit ticket <id>`.
- **`AttractionType` enum** (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) creates the matching subclass (`create(id, name, capacity)`) and parses command/file text case-insensitively; `Attraction.getType()` returns it.
- **Syntax & persistence:** `ATTRACTION CREATE` accepts 4 or 5 tokens; no type → GENERAL. File lines become `Id;Nombre;Capacidad;Tipo`; 3-field lines load as GENERAL; unknown types are skipped as malformed.
- **Output:** existing `ATTRACTION STATUS` text unchanged (kept for E2 compatibility); HELP gains a separate entry for the typed syntax.

## Risks / Trade-offs

- [Raw/wildcard handling of `Attraction<?>` in controllers] → only through the runtime-checked `enter(AccessTicket)`; typed `admit` is used where the static type is known (tests, future rules).
