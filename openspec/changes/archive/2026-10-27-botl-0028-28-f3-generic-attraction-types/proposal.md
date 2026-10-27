# Proposal

## Why

Milestone F3-C07 introduces attraction types — GENERAL, INDIVIDUAL, GROUP and SPECIAL — that restrict which ticket types can be used to enter, designed with access-capability interfaces implemented by the tickets and attractions with bounded generic types.

## What Changes

- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; the Delivery 2 form without type still works and creates a GENERAL attraction.
- Access capabilities as interfaces: general access (any ticket), individual access (individual tickets) and group access (group tickets); individual and group tickets implement the matching capability.
- Attractions become a generic hierarchy `Attraction<T extends AttractionAccess>` whose entry operation accepts only `T`: GENERAL → any ticket, INDIVIDUAL → individual tickets, GROUP → group tickets, SPECIAL → individual tickets (its Special Pass rule is its own behaviour, added with pass consumption in F3-C08).
- `ATTRACTION ENTER` rejects tickets whose type the attraction does not accept, keeping every Delivery 2 check.
- The attraction type is shown by `ATTRACTION STATUS` and stored with the attraction; stored attractions without a type load as GENERAL.
- Commit with the exact message `[F3-C07] Add generic attraction access types`.

## Capabilities

### New Capabilities
- `attraction-types`: The four attraction types, their accepted ticket types and the interface/generic design.

### Modified Capabilities
- `park-persistence`: attraction type stored; untyped stored attractions read as GENERAL.

## Impact

- New `business.model.{AttractionAccess, IndividualAccess, GroupAccess, AttractionType, GeneralAttraction, IndividualAttraction, GroupAttraction, SpecialAttraction}`; `Attraction` becomes abstract and generic; `AttractionController`, `AttractionCommands`, `AttractionFileStore`, help catalogue, UML diagram (kept consistent with the code) and tests updated.
- Out of scope here (F3-C08): the `SPEED` entry variant, SpeedPass consumption and the Special Pass requirement/consumption at SPECIAL attractions.
- Ambiguities flagged: type names are matched exactly in upper case; STATUS adds a `Type:` line after the name.
