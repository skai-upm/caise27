# Tasks

## 1. Persistence verification

- [x] 1.1 Add `Delivery3PersistenceIntegrationTest` (typed attractions, passes with price/consumed state, cascade deletion across restart, no occupants after restart, same output in normal and `--fly` mode and no leftovers); verify it passes

## 2. UML

- [x] 2.1 Update `uml/model.puml` to the final Delivery 3 design (interfaces, generics, attraction types, pass cardinalities, Builder, singleton controllers, DI relationships); verify with the PlantUML syntax check
- [x] 2.2 Extend `UmlModelTest` to the business packages and persistence implementations; verify it passes

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS)
- [x] 3.2 Commit with the exact message `[F3-C09] Extend delivery 3 persistence and UML`; verify with `git log -1 --format=%s`
