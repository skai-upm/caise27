# Proposal

## Why

Milestone F3-C07 (Delivery 3, step 7) introduces attraction types — GENERAL, INDIVIDUAL, GROUP and SPECIAL — that restrict which ticket types may be used for entry, and requires the design to express those restrictions with interfaces and bounded generic types that actually constrain what each attraction handles.

## What Changes

- `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` with type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; the Delivery 2 form without type still works and creates a GENERAL attraction; stored attractions without a type load as GENERAL.
- Ticket access capabilities as interfaces (general admission, individual access, group access) implemented by individual and group tickets.
- Attractions as a generic hierarchy bounded by those capabilities: GENERAL accepts individual and group tickets, INDIVIDUAL only individual tickets, GROUP only group tickets, SPECIAL only individual tickets and additionally requires an unconsumed Special Pass on the ticket (checked here; its automatic consumption and SpeedPass use at entry come in F3-C08).
- `ATTRACTION ENTER` rejects tickets the attraction does not accept, without changing anything.
- The attractions file stores the type.

### In scope
- Source sections 10, 10.1–10.4 (ticket-type acceptance and Special Pass requirement) and 11.

### Out of scope
- The `SPEED` entry option and pass consumption (F3-C08).

## Capabilities

### New Capabilities
- `attraction-types`: attraction types, accepted ticket types, and their generic, interface-based design.

### Modified Capabilities
- `attractions`: creation accepts an optional type.
- `park-persistence`: attractions are stored with their type; untyped records load as GENERAL.
- `execution-control`: `HELP` shows the optional type in `ATTRACTION CREATE`.

## Impact

- `Attraction` becomes `Attraction<T extends Admission>` with four subclasses and `AttractionType`; new interfaces `Admission`, `IndividualAccess`, `GroupAccess`, `PassLookup`; `AttractionService`, `AttractionFileStore`, `CommandProcessor` (variable field count), catalog, README, UML and its test.
