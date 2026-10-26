# Tasks

## 1. Command integration

- [x] 1.1 Add `CommandProcessorTest` checks: every registry entry is dispatched, unregistered operations are rejected, HELP and HELP EXTENDED list the E3 commands

## 2. Execution modes and persistence

- [x] 2.1 Add `AppTest` combined E1+E2+E3 script run interactively, from a file and with `--fly`, comparing outputs; verify `--fly` leaves `data/` untouched
- [x] 2.2 Verify restart with normal persistence restores users, groups, tickets, passes and attraction types in `AppTest`

## 3. Build and model

- [x] 3.1 Run `mvn package`, execute the combined script with `java -jar target/parque.jar` (file and `--fly`), and confirm `UmlModelTest` and `plantuml -checkonly uml/model.puml` pass
