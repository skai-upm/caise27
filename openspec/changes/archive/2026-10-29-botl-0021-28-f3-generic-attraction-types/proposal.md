# Proposal

## Why

Milestone F3-C07 adds attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL that restrict which tickets can be used to enter, and requires the design to express those restrictions with access-capability interfaces implemented by the tickets and bounded generic attraction types whose parameter really determines the tickets an attraction handles.

## What Changes

- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with type GENERAL, INDIVIDUAL, GROUP or SPECIAL; the Delivery 2 form without type keeps working and creates a GENERAL attraction. Stored attractions without a type load as GENERAL.
- Access restrictions on `ATTRACTION ENTER` (normal entry): GENERAL accepts individual and group tickets; INDIVIDUAL only individual tickets; GROUP only group tickets; SPECIAL only individual tickets and only if the ticket has an unconsumed Special Pass.
- Ticket access capabilities are interfaces (`AccessTicket`, `IndividualAccess`, `GroupAccess`) implemented by individual and group tickets; attractions are `Attraction<T extends AccessTicket>` subclasses (`GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`), whose typed entry operation only accepts `T`.
- `ATTRACTION STATUS` also shows the attraction type; the type is persisted.
- HELP / HELP EXTENDED list the typed creation syntax.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: attraction types, typed creation, type-based access restrictions, persistence of the type.
- `command-interface`: HELP lists the typed ATTRACTION CREATE syntax.

## Impact

- `business.model` (capability interfaces, generic `Attraction` hierarchy, `AttractionType`), `ParkController`, `TextFileAttractionRepository`, `CommandProcessor`, catalog, tests, UML.

## Ambiguities and unmet prerequisites (flagged)

- **SpeedPass use and Special Pass consumption** on entry belong to the next milestone (F3-C08). Here SPECIAL attractions check that an unconsumed Special Pass exists but do not consume it, and no entry option for SpeedPasses exists yet.
- Type names are matched exactly in upper case, as written in the source.
- With a SPECIAL attraction and a group ticket, or a GROUP attraction and an individual ticket, entry is rejected with an error stating the attraction type.
