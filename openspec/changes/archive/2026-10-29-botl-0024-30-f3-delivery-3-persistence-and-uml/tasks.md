# Tasks

## 1. Persistence verification

- [x] 1.1 Add `Delivery3PersistenceTest` (seeded data directory, command scripts through `App.run`, restart checks for types, pass tickets/prices/consumed states, cascade, empty attractions, and the same script with `--fly` leaving no data); verify it passes, fixing any persistence defect found

## 2. UML

- [x] 2.1 Add pass cardinalities to `uml/model.puml` and the Delivery 3 checklist to `UmlModelTest`; run `mvn clean package` and verify `BUILD SUCCESS`
