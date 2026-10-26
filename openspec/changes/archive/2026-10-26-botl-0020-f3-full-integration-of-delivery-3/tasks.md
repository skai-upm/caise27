# Tasks

## 1. Integration suite

- [x] 1.1 Write `delivery3.txt` (E1+E2+E3 commands) and `delivery3.expected`, and `DeliveryIntegrationTest` running it in file, interactive and `--fly` modes (persistence files present in normal mode, nothing left in fly mode, HELP and HELP EXTENDED complete); verify `mvn test`
- [x] 1.2 Run `mvn package` and `java -jar target/parque.jar --fly src/test/resources/integration/delivery3.txt`; verify the output equals `delivery3.expected`

## 2. Documentation and model

- [x] 2.1 Rewrite `README.md` with build, execution modes and help commands; verify it mentions every item of the README requirement
- [x] 2.2 Check `uml/model.puml` against the final code; verify `plantuml -checkonly` and the field cross-check pass
