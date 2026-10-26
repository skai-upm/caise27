# Proposal

## Why

All attractions currently accept any ticket. Delivery 3 specializes them by the kind of ticket they accept, using interfaces for ticket access capabilities and bounded generics so that each attraction type is restricted, at the type level, to the tickets it may handle.

## What Changes

- Four attraction types: `GENERAL` (individual and group tickets), `INDIVIDUAL` (individual tickets only), `GROUP` (group tickets only) and `SPECIAL` (individual tickets; its Special Pass rules are added with pass-based access in C08).
- `ATTRACTION CREATE;<id>;<name>;<capacity>` keeps working and creates a `GENERAL` attraction; the new `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` sets the type explicitly.
- Entering with a ticket kind the attraction does not admit is rejected.
- Interfaces represent access capabilities (`Accessible`, `IndividualAccess`, `GroupAccess`); `Attraction<T extends Accessible>` and its subclasses bind `T` to the capability they accept, and entry is typed on `T`.
- `ATTRACTION STATUS` shows the type; the type is stored with each attraction so it survives restarts (older files without a type load as `GENERAL`).
- PlantUML model and HELP updated.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: attraction types, typed creation, admission by ticket kind, status and persistence of the type.

## Impact

- New `Accessible`, `IndividualAccess`, `GroupAccess` interfaces and `AttractionType` enum; `Attraction` becomes generic and abstract with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`; `Ticket`/`IndividualTicket`/`GroupTicket` implement the capabilities; `AttractionController`, `AttractionRepository`, `CommandProcessor`, `CommandCatalog`, tests, `uml/model.puml`.
