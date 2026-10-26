# Tasks

## 1. Reference scenario

- [x] 1.1 Write `examples/input_fase3.txt` covering every E1, E2 and E3 command family (including expected rejections and a command after `EXIT`); verify by running it with `java -jar target/parque.jar --fly examples/input_fase3.txt`

## 2. Integration tests

- [x] 2.1 Add `FullIntegrationTest`: file, stdin and `--fly` runs give identical output with only the expected `ERROR` lines; normal-mode restart keeps E3 state; `--fly` writes nothing to the data directory; verify it passes
- [x] 2.2 Add the help-coverage check (every processor command family appears in `HELP` and `HELP EXTENDED`, and every catalog syntax is recognised); verify it passes and run the full `mvn test`

## 3. Packaging and model

- [x] 3.1 Verify `mvn package` builds `target/parque.jar` and the JAR runs the script in normal and `--fly` mode leaving no `tmp/<timestamp>` directory; verify `uml/model.puml` passes `plantuml -checkonly` and both model/code cross-checks
