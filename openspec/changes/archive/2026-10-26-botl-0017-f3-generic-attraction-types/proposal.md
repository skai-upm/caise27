# Proposal

## Why

Every attraction currently accepts any ticket. The park needs attractions reserved for individual visitors, for groups, or for Special Pass holders, and Delivery 3 asks to express which ticket kinds an attraction handles with access interfaces and bounded generics, enforced by the type system rather than by ad-hoc checks.

## What Changes

- Attraction types `GENERAL` (individual and group tickets), `INDIVIDUAL` (individual tickets only), `GROUP` (group tickets only) and `SPECIAL` (individual tickets that hold an available Special Pass, which is consumed on entry).
- Syntax: `ATTRACTION CREATE;Id;Nombre;Capacidad` keeps working and creates a GENERAL attraction; new `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` sets the type explicitly (case-insensitive; unknown type → error).
- Access capability interfaces `IndividualAccess` and `GroupAccess` (both extending `Admission`), implemented by `IndividualTicket` (standalone or internal) and `GroupTicket`.
- `Attraction<T extends Admission>` admits only `T` (rejecting other ticket kinds) and passes the typed admission to its access hooks; `SpecialAttraction extends Attraction<IndividualAccess>` uses the typed ticket to apply the Special Pass rule.
- The type is persisted (old files without type load as GENERAL) and shown by `ATTRACTION STATUS`.
- HELP catalog and UML updated.
- Required commit title: `[F3-C07] Add generic attraction access types`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `attraction-management`: attraction types, typed creation syntax, type-restricted entry, SPECIAL rule, type in status and persistence.

## Impact

- Model: `IndividualAccess`, `GroupAccess`, `AttractionType`, generic `Attraction<T>`, `SpecialAttraction`; `AttractionManager`, `AttractionStorage`, `ParkController`, `CommandProcessor`, `CommandCatalog` updated; tests adapted to the typed factory.
