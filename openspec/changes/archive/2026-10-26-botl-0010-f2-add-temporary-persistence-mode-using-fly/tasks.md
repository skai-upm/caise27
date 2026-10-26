# Tasks

## 1. Data directory

- [x] 1.1 Create `DataDirectory` (permanent/temporary factories, timestamp name with collision suffix, `resolve`, idempotent recursive `close`); verify with `DataDirectoryTest` (name format, uniqueness, deletion with files, permanent not deleted)

## 2. Application wiring

- [x] 2.1 Refactor `App` to parse `--fly`/command file, build every storage from one `DataDirectory`, close it on every termination path and add a shutdown hook in `main`; add `tmp/` to `.gitignore`; verify with `AppTest` (fly isolates and cleans up on EXIT, EOF and file mode; nonexistent file in fly mode cleans up; normal mode unchanged) and by running `java -jar target/parque.jar --fly` manually
- [x] 2.2 Update `uml/model.puml` with `DataDirectory` and its usage by `App`; verify `plantuml -checkonly` and the field cross-check pass, and `mvn test` passes
