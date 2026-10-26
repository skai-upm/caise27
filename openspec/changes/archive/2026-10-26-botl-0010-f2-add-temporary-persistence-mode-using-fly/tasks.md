# Tasks

## 1. Options and data directory

- [x] 1.1 Create `ExecutionOptions.parse` (`--fly` flag, first other argument as input file); verify `ExecutionOptionsTest` for `[]`, `[file]`, `[--fly]`, `[--fly, file]`, `[file, --fly]`
- [x] 1.2 Create `DataDirectory` (`createTemporary` with timestamp name and collision suffix, `deleteRecursively`); verify `DataDirectoryTest` with a fixed clock (`tmp/20260826_190315`, `_1` on collision, nested files deleted, missing directory ignored)

## 2. Application wiring

- [x] 2.1 Refactor `App`: relative file-name constants, `createProcessor(dataDir, controller)`, `start(inputFile, in, out)`, `execute(args, baseDir, clock, in, out)` with deletion in `finally` and a temporary shutdown hook; add `tmp/` to `.gitignore`; verify `FlyModeTest` that a `--fly` run writes inside its temporary directory, leaves `data/` unchanged, deletes the directory after EXIT, after end of input, after a file and after an invalid file, and that normal mode still persists to `data/` and creates no `tmp/`

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` (App attributes, `ExecutionOptions`, `DataDirectory` and their usage); verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run `mvn package` and end-to-end: `java -jar target/parque.jar --fly` (observing `tmp/<timestamp>` during the run and its removal afterwards) and `--fly input_fase2.txt`, then a normal run, checking `data/` is only modified in normal mode
