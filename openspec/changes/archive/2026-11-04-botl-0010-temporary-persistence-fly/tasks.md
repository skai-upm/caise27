# Tasks

## 1. Fly mode in App

- [x] 1.1 Parse a leading `--fly`, create `tmp/<yyyyMMdd_HHmmss>[_n]` next to the users file, run with `<dir>/users.txt` as the users file, and delete the directory tree in `finally` (plus a JVM shutdown hook registered while the fly run is active); add `tmp/` to `.gitignore`; verify with `AppTest` cases (all five data files written only under the temp dir while running, permanent files unchanged, independent start, directory removed after EXIT, after end of input and after a missing command file, `--fly <file>` output equals a normal run, normal run creates no `tmp/`)

## 2. Integration checks

- [x] 2.1 Run `mvn clean test` and run the packaged JAR with `--fly` (interactive via pipe and with a file) next to existing permanent data, confirming the permanent files are unchanged and `tmp/` holds no leftover run directory
