# Tasks

## 1. Pass-aware entry

- [x] 1.1 Add `getIndividualTickets()` to the access capability and tickets; turn `Attraction.enter` into the validate-then-mutate template with the default SpeedPass step and the `SpecialAttraction` override; verify unit tests: individual SPEED consumes one, group SPEED consumes one per member, missing member SpeedPass is atomic, special pass consumed/missing/used, SPEED rejected at SPECIAL, full attraction consumes nothing

## 2. Controller and commands

- [x] 2.1 Inject `PassController` into `AttractionController`, persist consumption, support `;SPEED` in `ATTRACTION ENTER` with consumption reported, update HELP and `App.initialize` order; verify command tests for each scenario and a restart test showing persisted consumption, then run `mvn test`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` (`AttractionController` → `PassController`); verify `plantuml -checkonly` and the model/code cross-check pass
