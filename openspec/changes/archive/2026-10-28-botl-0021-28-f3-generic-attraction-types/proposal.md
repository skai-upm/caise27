# Proposal

## Why

Milestone F3-C07 introduces attraction types — GENERAL, INDIVIDUAL, GROUP and SPECIAL — that restrict which tickets can be used for entry, and requires the design to express those restrictions with ticket-capability interfaces and attractions using bounded generic types (with a real effect on their operations).

## What Changes

- Add ticket access capability interfaces (general, individual and group access); individual tickets implement individual access and group tickets group access.
- Make attractions generic over the access capability they accept: GENERAL accepts any ticket, INDIVIDUAL individual tickets only, GROUP group tickets only, SPECIAL individual tickets only and additionally requires an unconsumed Special Pass for the ticket (its own entry rule, same ticket hierarchy as INDIVIDUAL).
- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 4-field form stays valid and creates a GENERAL attraction. Invalid types are rejected.
- `ATTRACTION ENTER` rejects tickets the attraction type does not accept; `ATTRACTION STATUS` shows the type.
- Persist the attraction type; stored attractions without a type load as GENERAL.
- Extend HELP/HELP EXTENDED and the UML model.
- Commit with the exact message `[F3-C07] Add generic attraction access types`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attractions`: ATTRACTION CREATE accepts an optional type; attraction types and their generic restriction.
- `attraction-access`: entry restricted by attraction type; SPECIAL requires an unconsumed Special Pass.
- `park-persistence`: attraction type stored, missing type read as GENERAL.
- `execution-control`: HELP lists the 5-field ATTRACTION CREATE.

## Impact

- `business.model`: capability interfaces, `AttractionType`, generic abstract `Attraction<T>` and four subclasses; controller creation/entry; attraction repository format; command field-count handling; UML and UML test (interfaces/enums); tests.
- **Flagged ambiguities / scope:**
  - Optional SpeedPass entry (`;SPEED`) and automatic Special Pass consumption belong to F3-C08. Here SPECIAL attractions only verify that the ticket has an unconsumed Special Pass; consumption is added in F3-C08.
  - Type names are matched exactly (upper case), as given in the source.
