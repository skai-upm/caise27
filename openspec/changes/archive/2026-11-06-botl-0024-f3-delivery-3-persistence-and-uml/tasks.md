# Tasks

## 1. Persistence verification

- [x] 1.1 Add `Delivery3PersistenceTest` covering restart of attraction types, passes (ticket, composed ticket, price, consumed), consumed passes kept, refunded and cascaded passes gone, and a `--fly` run leaving no data, and verify that it passes (fixing any gap found)

## 2. UML

- [x] 2.1 Add SpeedPass `0..*` / Special Pass `0..1` cardinalities to `uml/model.puml` and the Delivery 3 assertions to `UmlModelConsistencyTest`, and verify with the PlantUML syntax check and `mvn clean package`
