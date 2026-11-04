# Tasks

## 1. Temporary directory

- [x] 1.1 Create `TemporaryDataDirectory` (timestamped `tmp/yyyyMMdd_HHmmss` with `_n` suffix, recursive delete); verify with `TemporaryDataDirectoryTest`

## 2. Launch option

- [x] 2.1 Add `DataFiles.in(dir)`, `App.launch` (strip `--fly`, choose directory, run, delete in `finally`, shutdown hook) and use it from `main`; verify with `AppTest` (interactive and file `--fly` runs use the temp dir, permanent files unchanged, directory deleted after EXIT/EOF/missing file, normal mode creates no `tmp`)
- [x] 2.2 Add `TemporaryDataDirectory` to `uml/model.puml` (usage from `App`); verify `UmlModelTest` and the PlantUML `-syntax` check

## 3. Regression

- [x] 3.1 Run `mvn clean package` with all tests passing and manual `java -jar ... --fly` and `--fly input.txt` runs confirming no permanent file changes and no leftover run directory
