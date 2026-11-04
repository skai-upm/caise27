# Proposal

## Why

All attractions admit any ticket. Delivery 3 introduces attraction types that accept only certain ticket kinds, and asks that this restriction be modelled with access-capability interfaces and bounded generics rather than runtime switches.

## What Changes

- Attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`, each a subclass of a generic `Attraction<T extends AttractionAccess>`.
- New capability interfaces `AttractionAccess` (common), `IndividualAccess` (implemented by `IndividualTicket`) and `GroupAccess` (implemented by `GroupTicket`).
- `ATTRACTION CREATE;Id;Nombre;Capacidad` keeps creating `GENERAL`; `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` sets the type.
- Admission matrix: GENERAL individual+group, INDIVIDUAL individual, GROUP group, SPECIAL individual (its Special Pass rules come with the pass-based access change).
- `attractions.txt` stores the type as a fourth field; old three-field lines load as GENERAL.
- `ATTRACTION STATUS` also shows the type; `HELP` lists the typed creation form; UML updated.

## Capabilities

### New Capabilities
- `attraction-types`: types, admission matrix, interfaces/generics, status type.

### Modified Capabilities
- `attractions`: optional type in creation; type stored in the file; legacy lines read as GENERAL.
- `application-control`: `HELP` lists `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`.

## Impact

- `business.model`: `Attraction` becomes abstract generic; new `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, `AttractionAccess`, `IndividualAccess`, `GroupAccess`. `Ticket`/`IndividualTicket`/`GroupTicket` implement the capabilities.
- `AttractionManager`, `AttractionFileRepository`, `ParkController`, `CommandProcessor`, `CommandCatalog` updated.
- Delivery milestone message: `[F3-C07] Add generic attraction access types`.
