# Proposal

## Why

Milestone F3-C07 introduces the attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL, which restrict the kind of ticket allowed in. The design must use capability interfaces implemented by the tickets and bounded generic attraction types whose type parameter actually restricts accepted tickets. SPECIAL attractions accept individual tickets that hold an unconsumed Special Pass.

## What Changes

- Add access capability interfaces `GeneralAccess`, `IndividualAccess` (implemented by `IndividualTicket`) and `GroupAccess` (implemented by `GroupTicket`). Both sub-interfaces extend `GeneralAccess`.
- `Attraction` becomes `abstract class Attraction<T extends GeneralAccess>` with:
  - `GeneralAttraction` (`GeneralAccess`)
  - `IndividualAttraction` (`IndividualAccess`)
  - `GroupAttraction` (`GroupAccess`)
  - `SpecialAttraction` (`IndividualAccess`, plus its own Special Pass check)
- An `AttractionType` enum creates the right subclass.
- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` is added. The 4-field form remains and creates GENERAL (**MODIFIED** creation requirement).
- `ATTRACTION ENTER` rejects ticket kinds the attraction doesn't accept. SPECIAL requires an unconsumed Special Pass. `ATTRACTION STATUS` shows the type.
- Persistence stores the type, and lines without one load as GENERAL (**MODIFIED** `park-persistence`).
- `HELP` / `HELP EXTENDED` list the typed creation syntax.

### Out of scope (next milestone)
- `ATTRACTION ENTER ...;SPEED`, consumption of SpeedPasses and Special Passes on entry. Until then, Special Pass entry only checks that an unconsumed pass exists.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: typed creation, kind restrictions by type, Special Pass requirement, capability-based generic design.
- `park-persistence`: the attraction type is persisted.

## Impact

- New `GeneralAccess`, `IndividualAccess`, `GroupAccess`, `AttractionType` and four attraction subclasses.
- `Attraction` becomes abstract and generic.
- `ParkController` entry and creation.
- `AttractionCommands`, `AttractionRepository`, catalog, UML and tests.
