# Tasks

## 1. End-to-end verification

- [x] 1.1 Create `samples/input_fase3.txt` and `Delivery3IntegrationTest` (file mode responses, identical standard-input run, restart state, `--fly` equality and isolation, HELP/HELP EXTENDED coverage and dispatch); verify `mvn clean test`, fixing only integration defects found

## 2. Release checks

- [x] 2.1 Run `mvn clean package` and the JAR outside the IDE in a clean directory with the sample file in normal and `--fly` mode; verify the UML field-by-field and with `plantuml -checkonly`; record results in the verification matrix
