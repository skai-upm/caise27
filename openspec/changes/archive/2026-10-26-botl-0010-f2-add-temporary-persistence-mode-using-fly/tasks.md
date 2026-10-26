# Tasks

## 1. Temporary directory

- [x] 1.1 Create `TemporaryDataDirectory` (timestamped creation with collision suffix, idempotent recursive delete); verify with unit tests using `@TempDir`

## 2. Application wiring

- [x] 2.1 Refactor `App` to build all repositories from one data directory (`buildProcessor(Path)`), parse `--fly` and the command file, and clean up in `finally` plus a shutdown hook; add `tmp/` to `.gitignore`; verify `mvn test` passes
- [x] 2.2 Verify with the jar: `--fly` interactive and `--fly <file>` runs leave `data/` untouched and remove `tmp/<timestamp>`; a normal run still persists in `data/`
