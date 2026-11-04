# Tasks

## 1. End-to-end scenario

- [x] 1.1 Add `src/test/resources/integration/delivery3.txt` covering all E1/E2/F3 command families with an explicit system date; verify it runs through the packaged JAR with exit status 0
- [x] 1.2 Add `Delivery3IntegrationTest` (expected result per line and exact purchase/consumption literals in file mode; identical output interactive/file/`--fly`; restart restores final state; `--fly` leaves permanent files untouched; HELP and HELP EXTENDED list every operation); verify it passes

## 2. Defects and final verification

- [x] 2.1 Fix any integration defect found by 1.x (record it in this task if any; none expected from the exploratory run), then run `mvn clean package` with the full suite and the PlantUML `-syntax` check on `uml/model.puml` — result: no integration defects found (the two initial test failures were wrong test expectations, corrected in the test); 332 tests pass; PlantUML syntax OK; packaged JAR gives identical output in file, interactive and `--fly` modes
