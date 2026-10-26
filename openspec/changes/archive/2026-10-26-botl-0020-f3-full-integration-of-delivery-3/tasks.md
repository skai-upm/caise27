# Tasks

## 1. Scripts and integration tests

- [x] 1.1 Write `examples/input_basico.txt`, `examples/input_fase2.txt`, `examples/input_fase3.txt`; verify each runs with the jar (`--fly`) with no `Unknown command` or usage errors
- [x] 1.2 Add `AppIntegrationTest` (scripts through `App.buildProcessor` + restart checks) and the catalog-completeness test; verify `mvn test` passes

## 2. Final verification

- [x] 2.1 Verify `mvn package` builds `target/parque.jar` and that interactive, file, `--fly` and normal-persistence runs work; verify `uml/model.puml` with the cross-check script and `plantuml -checkonly`
