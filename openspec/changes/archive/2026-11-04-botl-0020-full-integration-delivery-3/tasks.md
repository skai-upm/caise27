# Tasks

## 1. End-to-end script and test

- [x] 1.1 Write `samples/input_fase3.txt` covering E1, E2 and F3 commands with expected successes and rejections; verify it runs with the packaged JAR and ends with `OK: Exiting`
- [x] 1.2 Write `Delivery3IntegrationTest` (file-mode responses, stdin and `--fly` equivalence, `--fly` isolation, restart state, HELP/HELP EXTENDED coverage vs. processor commands); verify it passes, fixing only demonstrated integration defects

## 2. Release verification

- [x] 2.1 Run `mvn clean package`, execute the shaded JAR outside the build (normal, `--fly`, interactive, missing file) in an empty directory, and run `plantuml -checkonly uml/model.puml`; verify all succeed and the full test suite passes
