# Proposal

## Why

All attractions admit any ticket. The park needs attractions restricted by the kind of ticket they accept, modelled in the type system — access-capability interfaces and bounded generics — rather than with scattered runtime switches.

## What Changes

- Attraction types `GENERAL` (individual and group tickets), `INDIVIDUAL` (individual only), `GROUP` (group only) and `SPECIAL` (individual tickets as basis; Special Pass rules come in F3-C08).
- Access-capability interfaces: `AccessTicket` (id, date, visitors), `IndividualAccess` (implemented by `Ticket`) and `GroupAccess` (implemented by `GroupTicket`).
- Generic `Attraction<T extends AccessTicket>` with subclasses `GeneralAttraction` (`AccessTicket`), `IndividualAttraction` and `SpecialAttraction` (`IndividualAccess`), `GroupAttraction` (`GroupAccess`); typed entry `admit(T)` plus a runtime-checked `enter(AccessTicket)` for tickets coming from commands.
- `ATTRACTION CREATE;Id;Nombre;Capacidad` still creates GENERAL; `ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo` sets the type. `ATTRACTION ENTER` rejects tickets the attraction does not admit.
- The attraction file stores the type; lines without type load as GENERAL.

## Capabilities

### New Capabilities
- `attraction-types`: the four types and which ticket kinds each admits.

### Modified Capabilities
- `attraction-catalog`: creation accepts an optional type; persistence stores it (default GENERAL).
- `application-control`: HELP lists the typed creation syntax.
- `domain-model-diagram`: diagram shows the access interfaces and generic attractions.

## Impact

- Model (`AccessTicket`, `IndividualAccess`, `GroupAccess`, `AttractionType`, attraction subclasses), `AttractionController`, `AttractionFileStorage`, `ParkController.enter`, `CommandProcessor`, `CommandCatalog`, tests, `uml/model.puml`.
