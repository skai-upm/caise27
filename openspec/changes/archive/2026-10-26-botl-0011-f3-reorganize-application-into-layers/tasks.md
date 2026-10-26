# Tasks

## 1. Move and rename

- [x] 1.1 Move main classes into `presentation`, `business.model`, `business.controller`, `business.validation` and `persistence` with `git mv`, renaming `*Manager` to `*Controller`; rewrite package declarations and imports; verify `mvn compile` succeeds
- [x] 1.2 Move tests to the matching packages (renaming manager tests) and update `pom.xml` main class to `es.upm.parque.presentation.App`; verify `mvn test` passes with the same number of behaviour tests

## 2. Layering checks

- [x] 2.1 Add `ArchitectureTest` (packages match directories and layers, no default package, command processor free of persistence/file I/O, model free of file I/O); verify it passes
- [x] 2.2 Verify `mvn package` builds `target/parque.jar` and a piped `HELP`/`USER CREATE`/`EXIT` session gives the same output as before the move

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with the packages, renamed controllers and `TemporaryDataDirectory` (usage from `App`); verify `plantuml -checkonly` and the model/code cross-check pass
