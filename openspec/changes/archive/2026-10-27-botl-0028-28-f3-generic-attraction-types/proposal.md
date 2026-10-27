# Proposal

## Why

Milestone F3-C07 introduces attraction types — GENERAL, INDIVIDUAL, GROUP and SPECIAL — that restrict which kind of ticket may be used to enter, designed with ticket-capability interfaces and bounded generic attraction types so that each attraction's generic parameter really limits the tickets it handles.

## What Changes

- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with type GENERAL, INDIVIDUAL, GROUP or SPECIAL; the Delivery 2 form without type still creates a GENERAL attraction.
- GENERAL accepts individual and group tickets; INDIVIDUAL only individual tickets; GROUP only group tickets; SPECIAL only individual tickets that have an unconsumed Special Pass (consuming it on entry and SpeedPass use belong to F3-C08).
- Ticket capabilities are interfaces implemented by the ticket classes; attractions are generic types bounded by those interfaces.
- Stored attractions keep their type; stored attractions without a type load as GENERAL.
- HELP lists the typed `ATTRACTION CREATE` syntax as well.
- Record the mandatory milestone commit `[F3-C07] Add generic attraction types` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-management`: attraction types and ticket-type restrictions on entry.
- `persistence`: attraction type stored; missing type means GENERAL.
- `command-processing`: HELP lists the typed creation syntax.

## Impact

- New `AccessTicket`, `IndividualAccess`, `GroupAccess`, `PassLookup`, `AttractionType`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`; `Attraction` becomes abstract and generic; `AttractionService`, `AttractionFileStorage`, `PassRepository`, `CommandProcessor`, `CommandCatalog`, tests and UML updated.
