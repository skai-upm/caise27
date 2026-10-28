# Proposal

## Why

Milestone F3-C07 introduces attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL that restrict which ticket types may be used for entry, designed with access-capability interfaces and bounded generics so an attraction's generic type actually limits the tickets it handles.

## What Changes

- `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` with type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; the Delivery 2 form without type keeps working and creates a GENERAL attraction.
- Access capability interfaces: `AccessTicket` (common), `IndividualAccess`, `GroupAccess`, `GeneralAccess`; `IndividualTicket` implements individual + general access, `GroupTicket` implements group + general access.
- `Attraction<T extends AccessTicket>` becomes an abstract generic class whose entry operation takes a `T`; subclasses `GeneralAttraction` (`GeneralAccess`), `IndividualAttraction` (`IndividualAccess`), `GroupAttraction` (`GroupAccess`), `SpecialAttraction` (`IndividualAccess`, additionally requiring an unconsumed Special Pass on the ticket as its own behavior).
- `ATTRACTION ENTER` rejects tickets the attraction type does not accept; entry checks (date, duplicates, capacity, all-or-nothing) move into the attraction.
- `attractions.txt` stores the type; lines without type are read as GENERAL. `ATTRACTION STATUS` shows the type.
- Commit with the mandatory message `[F3-C07] Add generic attraction access types`.

## Capabilities

### New Capabilities
- `attraction-types`: The four attraction types, their accepted ticket types, the Special Pass condition, and the generic/interface design.

### Modified Capabilities
- `park-persistence`: attractions are persisted with their type (missing type = GENERAL).

## Impact

- `business.model` (interfaces, generic attraction hierarchy, `AttractionType`, `PassLookup`), `AttractionService`, `AttractionFileStorage`, processor, UML and tests.
- Ambiguities flagged:
  - SpeedPass entry and pass consumption are F3-C08; in this milestone SPECIAL attractions require that the ticket has an unconsumed Special Pass but do not consume it yet.
  - Type names are matched exactly in uppercase; other values are rejected.
