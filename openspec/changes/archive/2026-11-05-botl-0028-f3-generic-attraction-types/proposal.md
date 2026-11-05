## Why

Delivery 3, milestone F3-C07 (source sections 10 and 11), introduces the attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL, which restrict the ticket types accepted on entry. The design must represent ticket access capabilities as interfaces and use bounded generic attraction types that actually restrict the tickets each attraction handles.

## What Changes

- **Access capabilities.** New interfaces in `business.model`:
  - `GeneralAccess`, implemented by `Ticket`;
  - `IndividualAccess extends GeneralAccess`, implemented by `IndividualTicket`;
  - `GroupAccess extends GeneralAccess`, implemented by `GroupTicket`.
- **Generic attractions.** `Attraction` becomes `abstract class Attraction<T extends GeneralAccess>`, with a `Class<T>` token.
  - `accept(ticket)` returns the ticket typed as `T`, or null.
  - `entryRefusal(T, PassLookup)` is the type-specific entry condition.
  - Subclasses: `GeneralAttraction` binds `GeneralAccess`, `IndividualAttraction` binds `IndividualAccess`, `GroupAttraction` binds `GroupAccess`, and `SpecialAttraction` binds `IndividualAccess` and requires an unconsumed Special Pass.
  - The `AttractionType` enum names the types and creates the matching subclass.
- **Pass lookup.** New model interface `PassLookup`, implemented by `PassRepository`, so attraction rules can query passes without the model depending on persistence.
- **Command.** `ATTRACTION CREATE;<id>;<name>;<capacity>[;<type>]`; without a type it creates a GENERAL attraction. The response names the type, and HELP shows the optional type.
- **Entry.** `ATTRACTION ENTER` rejects ticket types the attraction does not accept, and applies the Special Pass condition. Previous checks (existence, date, capacity, already inside, all-or-nothing group entry) are unchanged.
- **Persistence.** `attractions.txt` lines become `id;name;capacity;TYPE`. Lines without a type load as GENERAL, the compatibility rule in source section 10.
- **Out of scope** (F3-C08, proposal 29): the `SPEED` entry option, and consumption of SpeedPasses and Special Passes on entry. Until then, a Special Pass is checked but not consumed.

## Capabilities

### New Capabilities
- `attraction-types`: the ticket types accepted by each attraction type, the special attraction's Special Pass condition, and the generic/interface design.

### Modified Capabilities
- `attractions`: creation with a type, and individual and group access restricted by attraction type.
- `park-persistence`: attraction types stored and restored, with typeless lines loaded as GENERAL.
- `execution-control`: HELP shows the optional type in `ATTRACTION CREATE`.

## Impact

- `business.model`: new interfaces, the `AttractionType` enum and four attraction subclasses; `Attraction` becomes generic and abstract.
- `persistence`: `PassRepository` implements `PassLookup`; `ParkFileStore` and `AttractionRepository` change.
- `business`: `AttractionManager` changes.
- `presentation`: `AttractionCommands` and `CommandCatalog` change.
- UML, tests and README.
