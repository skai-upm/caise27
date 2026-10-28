# Tasks

## 1. Layered packages

- [x] 1.1 Move sources and tests to `presentation`, `business` (+`model`, `service`, `exception`) and `persistence` (+`repository`, `file`) and update the manifest main class; verify `mvn package` passes with all existing tests and the JAR starts
- [x] 1.2 Move the `--fly` directory creation/deletion into `persistence.file.DataLocation` driven by `ParkController`; verify presentation classes import nothing from `persistence` nor `java.nio.file.Files` (grep) and fly tests still pass

## 2. Exceptions

- [x] 2.1 Introduce the `ParkException` hierarchy and `CommandSyntaxException`, replace `OperationException` throw sites by the matching subtype and handle `ParkException` in `CommandProcessor`; verify unit tests asserting the exception type for not-found, conflict, not-allowed, validation and syntax cases, and unchanged `ERROR:` output

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` packages; verify `UmlModelConsistencyTest` passes

## 4. Milestone commit

- [x] 4.1 Commit with the exact message `[F3-C01] Reorganize application into layered packages`; verify with `git log -1 --format=%s`
