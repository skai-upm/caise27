# Tasks

## 1. Integration script and tests

- [x] 1.1 Write `examples/input_fase3.txt` combining E1, E2 and E3 commands with expected rejections; verify it runs end to end through the packaged JAR ending at EXIT
- [x] 1.2 Add `Delivery3IntegrationTest` (OK/ERROR sequence in file mode, interactive equals file output, restart keeps hashed password change, consumed passes and attraction types, `--fly` identical output without permanent data, every registered command in HELP and HELP EXTENDED); verify the full suite passes

## 2. Delivery checks

- [x] 2.1 Run `mvn package`, the JAR in interactive, file and `--fly` modes, the regression script, and validate `uml/model.puml` with PlantUML and the cross-check script; fix any defect found
