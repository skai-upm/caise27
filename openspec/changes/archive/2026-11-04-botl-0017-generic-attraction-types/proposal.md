# Proposal

## Why

All attractions accept every kind of ticket. Delivery 3 specialises attractions by the tickets they accept — GENERAL, INDIVIDUAL, GROUP and SPECIAL — modelling the accepted ticket capability in the type system with interfaces and bounded generics instead of scattered runtime switches.

## What Changes

- Ticket access capabilities as interfaces: `Admission` (identifier, date, visitors), refined by `IndividualAccess` (implemented by `IndividualTicket`) and `GroupAccess` (implemented by `GroupTicket`); the abstract `Ticket` implements `Admission`.
- `Attraction` becomes `abstract class Attraction<T extends Admission>` holding the accepted capability class `Class<T>`, used to decide whether a ticket is admitted and to obtain it typed as `T` before the typed entry operation runs.
- Four attraction types: `GeneralAttraction` (`Admission` — individual and group tickets), `IndividualAttraction` (`IndividualAccess`), `GroupAttraction` (`GroupAccess`), `SpecialAttraction` (`IndividualAccess`, with a hook for additional Special Pass conditions added by the access change). An `AttractionType` enum creates them.
- `ATTRACTION CREATE;Id;Nombre;Aforo` (E2 syntax) still works and creates a GENERAL attraction; new `ATTRACTION CREATE;Id;Nombre;Aforo;Tipo` with `Tipo` ∈ {GENERAL, INDIVIDUAL, GROUP, SPECIAL}.
- `ATTRACTION ENTER` with a ticket kind the attraction does not accept is rejected with `ERROR:` (nobody enters).
- `attractions.txt` lines gain a 4th field with the type; existing 3-field lines are read as GENERAL.
- `ATTRACTION STATUS` also shows the type. HELP catalog and UML updated.

## Capabilities

### New Capabilities
- `attraction-types`: the four types, accepted ticket kinds per type, and the capability-interface / bounded-generic design.

### Modified Capabilities
- `attractions`: "Attraction data" (type), "Create attraction" (optional type), "Attraction definitions persist, occupancy does not" (type field, legacy lines → GENERAL).
- `attraction-access`: "Attraction status" shows the type.

## Impact

- **Code**: model `Admission`, `IndividualAccess`, `GroupAccess`, `AttractionType`, generic `Attraction<T>` and four subclasses; `Ticket`/`IndividualTicket`/`GroupTicket` implement the interfaces; `AttractionService`, `AttractionRepository`, `ParkController`, `CommandProcessor`, `CommandCatalog`, UML, tests.
- **Data**: `attractions.txt` gains the type field (backward compatible on read).
- **Mandatory milestone message**: `[F3-C07] Add generic attraction access types`.
- **Assumptions recorded**: the type literal is case-sensitive uppercase; `ATTRACTION EXIT` keeps working for any ticket whose visitors are inside (type rules apply to entry); SPECIAL accepts exactly what INDIVIDUAL accepts in this change — its Special Pass condition is part of the attraction-access-with-passes change.
