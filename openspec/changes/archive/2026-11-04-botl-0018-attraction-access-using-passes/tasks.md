# Tasks

## 1. Attraction preflight and hooks

- [x] 1.1 Add `requireAdmissible`, `acceptsSpeed`, `requiresSpecialPass` to `Attraction` (SpecialAttraction overrides) and reuse the preflight in `admit`; verify existing attraction tests and new hook tests

## 2. Pass-aware entry

- [x] 2.1 Add `PassController.consume` (mark + save, undo on failure) and `ParkController.enter(attraction, ticket, speed)` with the check-then-commit sequence; support the `SPEED` option in `CommandProcessor` and the catalog; verify `PassEntryTest` for every brief scenario (single SpeedPass, group atomicity, capacity failure, Special Pass, SPEED at SPECIAL, invalid option, restart) and update the SPECIAL scenario of the attraction type tests

## 3. Verification

- [x] 3.1 Update `uml/model.puml` if structure changed; verify `plantuml -checkonly`, `mvn clean test` and the regression script (only HELP changes)
