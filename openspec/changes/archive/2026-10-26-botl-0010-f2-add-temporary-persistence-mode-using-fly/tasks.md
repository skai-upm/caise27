# Tasks

## 1. Temporary directory

- [x] 1.1 Create `TemporaryDataDirectory` (timestamped creation with unique suffix, recursive delete); verify unit tests for naming, collision suffix and deletion of nested files

## 2. Execution option

- [x] 2.1 Parse `--fly` and the command file in `App` (unknown options rejected), select the temporary data directory and delete it in `finally` plus a shutdown hook in `main`; verify tests: fly interactive run leaves permanent data untouched and `tmp` empty, fly with command file processes commands across repositories, unknown option error, normal mode still writes to the data directory
- [x] 2.2 Set `<finalName>parque</finalName>` and add `tmp/` to `.gitignore`; verify `mvn package` produces `target/parque.jar` and `java -jar target/parque.jar --fly` with piped commands leaves no `tmp/<timestamp>` directory
- [x] 2.3 Run `mvn test` and verify the full suite passes
