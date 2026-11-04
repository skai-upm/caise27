# Proposal

## Why

All attractions currently admit every ticket. Delivery 3 introduces attraction types restricted by the kind of ticket they accept. These restrictions must be modelled in the type system — interfaces for ticket access capabilities and bounded generics on attractions — rather than as scattered runtime switches.

## What Changes

- New access-capability interfaces in the model:
  - `Admissible` (identifier, date, holders), implemented by `Ticket`;
  - `IndividualAccess` (adds the single owner), implemented by `IndividualTicket`;
  - `GroupAccess` (adds the constituent tickets), implemented by `GroupTicket`.
- `Attraction` becomes the generic abstract `Attraction<T extends Admissible>` with typed `enter(T, today)`/`exit(T)`. It has four subclasses:
  - `GeneralAttraction` (`T = Admissible`);
  - `IndividualAttraction` (`T = IndividualAccess`);
  - `GroupAttraction` (`T = GroupAccess`);
  - `SpecialAttraction` (`T = IndividualAccess`; its Special Pass rules arrive with the pass-based access change).
- New enum `AttractionType` (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) creating the matching attraction.
- `ATTRACTION CREATE;Id;Nombre;Capacidad` still creates `GENERAL`; new `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` sets the type explicitly.
- `ATTRACTION ENTER` rejects ticket kinds the type does not accept.
- `attractions.txt` now stores the type; legacy 3-field lines load as `GENERAL`. `ATTRACTION STATUS` shows the type.
- HELP/catalog and the PlantUML model are updated.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `attractions`: creation with type, type persistence (legacy lines as GENERAL), type in status, new "Attraction types" requirement.
- `attraction-access`: new "Ticket kind accepted by attraction type" requirement.
- `application-control`: HELP lists the typed creation syntax.
- `uml-model`: access interfaces and generic attraction hierarchy shown.

## Impact

- Model: `Admissible`, `IndividualAccess`, `GroupAccess`, `AttractionType`, `Attraction<T>` and four subclasses; `Ticket`, `IndividualTicket`, `GroupTicket` implement the interfaces.
- `AttractionManager`, `AttractionFileStorage`, `ParkController`, `CommandProcessor`, `CommandCatalog`, tests, `uml/model.puml`, `UmlModelTest` (generic declarations).
