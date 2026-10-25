# Tasks

## 1. Baseline

- [x] 1.1 Build the current JAR and record the output of a command file covering users, groups, tickets, group tickets, attractions, SYSTEM DATE/PRICE, HELP and EXIT (normal and `--fly`) as the behavior baseline

## 2. Move production code

- [x] 2.1 Move `parsePrice` to `Ticket` and `parseCapacity` to `Attraction` (controllers delegate); verify `mvn test` passes
- [x] 2.2 Move classes into `presentation`, `business.model`, `business.controller`, `persistence` (App stays in `es.upm.parque`), fix imports and visibility; verify `mvn compile`

## 3. Tests and architecture checks

- [x] 3.1 Move tests to matching packages with shared public `Fixtures`/`Park`; verify `mvn test` passes with the same number of tests
- [x] 3.2 Add `LayeringTest` enforcing the layer dependency rules on source imports; verify it passes
- [x] 3.3 Update `uml/model.puml` with package blocks and `UmlModelTest` for multiple packages; verify `plantuml -checkonly` and the test

## 4. Integration

- [x] 4.1 `mvn package` and rerun the baseline command file with the new JAR; verify the output is identical
