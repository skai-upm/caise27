# Proposal

## Why

All attractions accept every ticket kind. Access restrictions must be part of the attraction type system: GENERAL, INDIVIDUAL, GROUP and SPECIAL attractions that accept only the ticket classes they are designed for, expressed with access-capability interfaces and bounded generics rather than scattered runtime switches.

## What Changes

- Four attraction types: GENERAL (individual and group tickets), INDIVIDUAL (individual tickets only), GROUP (group tickets only), SPECIAL (individual tickets only, as the basis for the Special Pass rules added by a later change).
- An access-capability interface implemented by tickets, and an attraction class generic over the credential type it admits (`Attraction<T extends AccessCredential>`), with one subclass per type binding `T` to `Ticket`, `IndividualTicket` or `GroupTicket`; admission is performed through the typed method.
- `ATTRACTION CREATE;Id;Nombre;Capacidad` keeps working and creates a GENERAL attraction; `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` sets the type explicitly.
- `ATTRACTION ENTER` with a ticket kind the attraction does not admit is rejected without changes.
- `attractions.txt` stores the type as a fourth field; existing three-field lines are read as GENERAL.
- HELP/HELP EXTENDED list the typed creation; UML updated. `ATTRACTION STATUS` output is unchanged.

The source suggests change ID `generic-attraction-types`; this batch uses the BOTL ID `botl-0017-17-f3-generic-attraction-types`. Mandatory milestone message: `[F3-C07] Add generic attraction access types`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: creation with optional type; new requirement on the ticket kinds admitted by each type.
- `attraction-file-persistence`: records include the type; untyped records are GENERAL.
- `application-control`: HELP lists `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`.

## Impact

- Model: `AccessCredential`, `AttractionType`, generic abstract `Attraction<T>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`; `Ticket` implements `AccessCredential`.
- `AttractionManager.create` with type; `AttractionFileStore` format; parsing, catalog and UML.
