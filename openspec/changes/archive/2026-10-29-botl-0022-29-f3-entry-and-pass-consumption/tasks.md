# Tasks

## 1. Entry model

- [x] 1.1 Add `individualTickets()` to the access capability, the richer `PassLookup`, the `Entry` record, and the pass-selection step in the `Attraction` template (default SpeedPass rule, `SpecialAttraction` Special Pass rule); verify with unit tests that every check precedes mutation

## 2. Operations and commands

- [x] 2.1 Update `ParkController.enterAttraction` (speed flag, lookups) and `CommandProcessor` (optional `SPEED`, output of consumed passes), catalog entry; verify with tests for every scenario of the spec (individual SPEED, none available, full attraction keeps passes, group SPEED all/none, special consumption, second entry rejected, SPEED at special rejected) and HELP
- [x] 2.2 Update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run of `ATTRACTION ENTER;A01;T001;SPEED`
