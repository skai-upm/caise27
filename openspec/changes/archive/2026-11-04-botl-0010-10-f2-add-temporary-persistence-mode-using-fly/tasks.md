# Tasks

## 1. Fly mode in the launcher

- [x] 1.1 Parse `--fly` in `App.run` (any position; first remaining argument is the command file), create the unique `tmp/<yyyyMMdd_HHmmss>` run directory under a configurable root, use it as data directory, and delete it recursively in `finally`; register an idempotent shutdown-hook cleanup in `main`; verify with `AppTest` (interactive fly run leaves normal files untouched and removes the run dir after EXIT, fly run with command file, fly with missing file cleans up, normal run creates no tmp dir, fly run starts empty, two fly runs in the same second get distinct directories)

## 2. Regression

- [x] 2.1 Verify `mvn test` passes and run the packaged JAR as `java -jar target/<jar> --fly` and `--fly input_fase2.txt` in a scratch directory with existing data files, confirming the files are unchanged and no run directory remains
