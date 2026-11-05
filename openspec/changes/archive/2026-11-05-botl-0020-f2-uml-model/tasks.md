# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` with packages, classes/enum with relevant attributes, Ticket hierarchy, GroupTicket→IndividualTicket composition, directed associations with multiplicities, ParkController→repositories and persistence classes; verify `java -jar plantuml.jar -checkonly uml/model.puml` succeeds

## 2. Consistency

- [x] 2.1 Add `UmlModelConsistencyTest` (code classes of model/repository/park/persistence declared in the diagram and vice versa; key inheritance/composition/association lines present; no association starting at `User`); verify `mvn test` passes
- [x] 2.2 Review every association against the holding field in the code and document the review result in the design (directions and multiplicities match)
