# Proposal

## Why

Milestone F3-C07 introduces attraction types — GENERAL, INDIVIDUAL, GROUP and SPECIAL — that restrict which ticket types may be used to enter, and requires the design to express these restrictions with ticket-access interfaces and bounded generic attraction types whose type parameter really limits the accepted tickets.

## What Changes

- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with `GENERAL|INDIVIDUAL|GROUP|SPECIAL`; the 4-element Delivery 2 syntax still works and creates a GENERAL attraction; stored attractions without a type load as GENERAL.
- Entry rules by type: GENERAL accepts individual and group tickets; INDIVIDUAL and SPECIAL accept individual tickets only; GROUP accepts group tickets only. A ticket type not accepted is rejected without changes.
- Design: access-capability interfaces implemented by individual and group tickets; attractions are generic types bounded by those interfaces and only admit tickets of their bound.
- `ATTRACTION STATUS` also shows the type; the attractions file stores the type.
- Out of scope (F3-C08): SpeedPass use (`;SPEED`) and the Special Pass requirement/consumption at SPECIAL attractions.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: attraction types and the extended creation syntax.
- `attraction-access`: entry restricted by attraction type.

## Impact

- `business.model`: `TicketAccess`, `IndividualAccess`, `GroupAccess`, generic abstract `Attraction<T>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`; `AttractionService`, `AttractionFileStorage`, `CommandProcessor` (optional element), UML.
