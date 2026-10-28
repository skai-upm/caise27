# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` with the model classes, attributes, hierarchy, composition, directed associations and multiplicities; verify with `java -jar plantuml.jar -checkonly uml/model.puml`

## 2. Consistency

- [x] 2.1 Add `UmlModelTest` checking classes and fields against the diagram in both directions; verify it passes in `mvn package`

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS)
- [x] 3.2 Commit with the exact message `[F2-C09] Add PlantUML class model`; verify with `git log -1 --format=%s`
