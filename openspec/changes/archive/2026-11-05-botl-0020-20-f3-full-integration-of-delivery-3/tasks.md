# Tasks

## 1. End-to-end verification

- [x] 1.1 Add `input_fase3.txt` and `DeliveryIntegrationTest` (file run on empty data with no errors and expected outputs/data, identical interactive output, `--fly` run leaving normal data unchanged and no run directory), fixing any integration defect found; verify `mvn test`
- [x] 1.2 Add the help coverage test (every catalog command recognised; present in HELP and HELP EXTENDED with description); verify it passes

## 2. Release

- [x] 2.1 Run `mvn clean package`, execute the JAR with `input_fase3.txt` normally and with `--fly` in a scratch directory, and verify PlantUML `-checkonly`, the code cross-check and the layer rules
