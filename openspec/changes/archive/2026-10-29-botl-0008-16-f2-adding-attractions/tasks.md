# Tasks

## 1. Model

- [x] 1.1 Create `Attraction` (identifier, name, capacity, ordered occupants, `addOccupant` refusing when full or duplicate, `removeOccupant`, unmodifiable view); verify with unit tests for capacity bound, duplicates and that `User` has no attraction field

## 2. Central management and commands

- [x] 2.1 Add attraction registry and `createAttraction` (unique id, non-blank id/name, positive whole capacity) to `ParkController`; verify with unit tests for the source examples and for `0`, `-1`, `abc`, duplicate id, blank name
- [x] 2.2 Dispatch `ATTRACTION CREATE` and `ATTRACTION LIST` in `CommandProcessor` and add both syntaxes to HELP; verify with processor tests listing two occupants, the empty result, an unknown attraction, state unchanged after LIST, and HELP output
- [x] 2.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with `ATTRACTION CREATE;A01;Dragon Rojo;20`, `ATTRACTION LIST;A01`, `ATTRACTION LIST;A99`, `EXIT` and verify the outputs
