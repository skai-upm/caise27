# Design

## Context

`TextUserRepository` (persistence layer, implements the `UserRepository` port injected into `ParkController`) writes `dni;email;hash;firstName;surnames;phone;card` and reads 2/3/7 fields. `User` and `App` have no file I/O (checked by `ArchitectureTest`).

## Decisions

- **Write** `firstName;surnames;dni;email;hash;phone;card` (empty hash when none).
- **Read**: 2 or 3 fields → earlier `dni;email[;hash]`; 7 fields → if field 4 (index 3) contains `@` it is the assignment order, otherwise (index 1 contains `@`) the previous order. Anything else is reported and skipped.
- No change to the controller or model; separation of responsibilities verified by the existing architecture test plus a check that `User` and `App` do not reference the user file.
