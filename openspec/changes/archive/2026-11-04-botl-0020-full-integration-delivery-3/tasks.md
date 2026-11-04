# Tasks

## 1. End-to-end scenario

- [x] 1.1 Write `src/test/resources/e2e/delivery3.txt`, generate its output with the packaged JAR, review every line against the specs, and commit it as `delivery3.expected.txt`; verify each command's result matches its specification (fix and record any integration defect)
- [x] 1.2 Add `IntegrationTest` (file mode, interactive mode, `--fly` isolation and cleanup, help coverage of every catalog syntax in both help modes); verify it passes

## 2. Release verification

- [x] 2.1 Run `mvn clean package`, run the JAR on the e2e file in an empty directory outside Maven and compare with the golden output; re-verify `uml/model.puml` (PlantUML `-syntax`, `UmlModelTest`, label/field check)
