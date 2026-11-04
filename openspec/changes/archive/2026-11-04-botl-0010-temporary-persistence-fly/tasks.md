# Tasks

## 1. Temporary directory handling

- [x] 1.1 Add `createFlyDirectory(base, start)` (timestamp `yyyyMMdd_HHmmss`, suffix on collision) and recursive `deleteFlyDirectory(dir)` (also removing empty `tmp/`) to `App`; verify `FlyModeTest` for naming, collision suffix and deletion

## 2. Launch integration

- [x] 2.1 Recognise `--fly` in `App.run`, select the temporary directory as data directory for all repositories, keep the first other argument as command file, and delete the directory in `finally`; register a shutdown hook in `main`; verify `FlyModeTest` (interactive and file fly runs, EXIT, missing file, permanent files byte-identical, independent starting state) and that existing `AppTest` normal-mode tests still pass

## 3. Integration verification

- [x] 3.1 Verify with the packaged JAR: `java -jar target/parque-1.0-SNAPSHOT.jar --fly` and `--fly <file>` leave no `tmp/` and do not touch permanent files, and a normal run still persists; `mvn test` and `mvn package` succeed
