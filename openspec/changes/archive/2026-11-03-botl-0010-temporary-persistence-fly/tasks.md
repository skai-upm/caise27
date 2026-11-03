# Tasks

## 1. Launch options

- [x] 1.1 Add `LaunchOptions` parsing in `App` (`--fly` anywhere, first other argument = command file); verify tests for `[]`, `[file]`, `[--fly]`, `[--fly, file]`, `[file, --fly]`

## 2. Temporary data directory

- [x] 2.1 Create `tmp/<yyyyMMdd_HHmmss>` (with `_n` suffix when taken), run interactive or file mode over it and delete it (and an empty `tmp/`) in `finally` and in a shutdown hook; add `tmp/` to `.gitignore`; verify tests that a `--fly` run writes only into the temporary directory, leaves permanent files unchanged, and removes the directory after EXIT, end of input and a missing command file

## 3. Integration verification

- [x] 3.1 Verify normal mode still writes the permanent files and creates no `tmp/`, the responses of a `--fly` run equal those of a normal run on empty data, `mvn clean test` passes and `java -jar target/amusement-park-1.0-SNAPSHOT.jar --fly input_fase2.txt` leaves no `tmp/` behind
