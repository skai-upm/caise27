# Proposal

## Why

Milestone F3-C07 introduces attraction types that restrict which tickets can be used to enter: GENERAL (individual and group tickets), INDIVIDUAL (individual tickets only), GROUP (group tickets only) and SPECIAL (individual tickets with an unconsumed Special Pass). The design must use ticket-capability interfaces and bounded generic attraction types whose type parameter actually restricts the accepted tickets. Today every attraction accepts every ticket.

## What Changes

- Add `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`. The four-field form remains valid and creates a GENERAL attraction. Unknown types are rejected.
- Represent ticket access capabilities as interfaces (general access, individual access, group access). Individual and group tickets implement the matching capability.
- Make attractions generic over the capability they accept. The bound determines which tickets an attraction can admit. `ATTRACTION ENTER` with a ticket the attraction's type does not accept is rejected without changing occupants.
- SPECIAL attractions additionally require that the individual ticket has an unconsumed Special Pass. Consuming that pass on entry, and using SpeedPasses at entry, belong to the entry-and-pass-consumption milestone.
- Persist the attraction type. Stored attractions without a type load as GENERAL.
- Show the type in ATTRACTION STATUS, and list the typed creation syntax in HELP.

### In scope
- Types, typed creation, capability interfaces, bounded generic attractions, acceptance rules, Special Pass requirement, persistence and status.

### Out of scope
- SpeedPass entry options and pass consumption (F3-C08).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: creation with an optional type, type-based acceptance of tickets, the Special Pass requirement of SPECIAL attractions, the type in status, and the HELP line.
- `park-persistence`: the attraction type is stored, and untyped data loads as GENERAL.

## Impact

- Model: new interfaces `AccessTicket`, `IndividualAccess`, `GroupAccess`; `Attraction<T extends AccessTicket>` becomes abstract, with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and the `AttractionType` enum.
- `ParkController` creation and entry delegate to the typed attraction. `AttractionRepository` stores the type. CommandProcessor and catalog are updated.
- Tests, UML, README.
