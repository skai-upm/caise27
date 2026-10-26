# Proposal

## Why

Attractions currently accept any ticket. Delivery 3 introduces attraction types that restrict the accepted ticket kind, and requires expressing those restrictions in the type system with interfaces for ticket access capabilities and bounded generics, not only with runtime checks.

## What Changes

- Four attraction types: `GENERAL` (individual and group tickets), `INDIVIDUAL` (only individual tickets), `GROUP` (only group tickets) and `SPECIAL` (individual tickets as the basis for access; its Special Pass rules are added by the next change).
- New syntax `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo`; the E2 syntax without type keeps working and creates a `GENERAL` attraction.
- Access capability interfaces: `AttractionAccess` (common: id, date, admitted users), `IndividualAccess` (implemented by individual tickets) and `GroupAccess` (implemented by group tickets).
- `Attraction<T extends AttractionAccess>` becomes a generic abstract class whose entry operation takes a `T`; each type is a subclass binding `T` (`GeneralAttraction` → `AttractionAccess`, `IndividualAttraction` and `SpecialAttraction` → `IndividualAccess`, `GroupAttraction` → `GroupAccess`). Tickets of another kind are rejected.
- `ATTRACTION STATUS` shows the type; the type is persisted with the attraction (old lines without type load as `GENERAL`).
- HELP/HELP EXTENDED and the UML model are updated.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attractions`: attraction types, typed creation syntax, accepted ticket kinds per type, type in status and persistence.
- `uml-model`: the model shows the access interfaces and the generic attraction hierarchy.

## Impact

- `business.model`: new interfaces and attraction subclasses, `AttractionType` enum, `Attraction` made generic; tickets implement the access interfaces. `AttractionController`, `FileAttractionRepository`, `CommandProcessor`, `CommandCatalog`, UML and tests updated.
