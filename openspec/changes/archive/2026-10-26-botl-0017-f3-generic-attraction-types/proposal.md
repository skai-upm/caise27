# Proposal

## Why

All attractions accept any ticket today. Delivery 3 specialises attractions by the kind of ticket they admit, and uses this to apply interfaces for access capabilities and bounded generics that restrict, at compile time and at run time, which ticket classes each attraction type handles.

## What Changes

- Four attraction types: GENERAL (individual and group tickets), INDIVIDUAL (individual tickets only), GROUP (group tickets only) and SPECIAL (individual tickets, base for the Special Pass rules of the next proposal).
- `ATTRACTION CREATE;<id>;<name>;<capacity>` keeps working and creates a GENERAL attraction; new form `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>`.
- Access capabilities as interfaces: `Admission` (what any ticket offers: id, date, users admitted), specialised by `IndividualAdmission` and `GroupAdmission`; tickets implement them.
- `Attraction<T extends Admission>` with a type token; each attraction type binds `T` (`Admission`, `IndividualAdmission`, `GroupAdmission`) and only admits tickets of that capability; others are rejected with `ERROR TICKET TYPE NOT ACCEPTED`.
- The type is shown by `ATTRACTION STATUS` and stored with the attraction (old files without a type load as GENERAL).
- HELP lists the typed creation syntax; UML shows interfaces, generics and the hierarchy.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attractions`: attraction data includes a type; entry checks the accepted ticket kind; status shows the type; persistence stores it; new requirement describing the four types.
- `command-processing`: HELP scenario lists the typed `ATTRACTION CREATE` syntax.

## Impact

- Model: `Admission`, `IndividualAdmission`, `GroupAdmission`, `AttractionType`, generic abstract `Attraction<T>` with four subclasses; `Ticket`/`IndividualTicket`/`GroupTicket` implement the interfaces. `AttractionManager`, repository, command registry, tests, UML and `UmlModelTest` (generic declarations, interface inheritance) updated.
