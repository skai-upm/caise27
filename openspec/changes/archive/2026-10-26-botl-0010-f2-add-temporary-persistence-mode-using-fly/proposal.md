# Proposal

## Why

Running scripted scenarios or experiments currently modifies the permanent data files under `data/`. A temporary mode is needed to exercise every persistence feature in isolation and leave the permanent state untouched.

## What Changes

- New execution option `--fly` (combinable with interactive mode and with a command file, e.g. `java -jar target/parque.jar --fly input_fase2.txt`).
- With `--fly`, the application creates at start-up a fresh directory `tmp/<yyyyMMdd_HHmmss>/` (unique per execution) and every repository (users, groups, tickets, group tickets, attractions and any future data) uses only files inside it.
- The temporary directory and all its files are deleted when the application terminates, including through `EXIT`, end of input or an interrupted JVM.
- Repositories are not duplicated: the mode only changes the configured data location. Without `--fly` the permanent persistence behaviour is unchanged.
- The packaged JAR is named `target/parque.jar` so the documented commands work as written.

## Capabilities

### New Capabilities
- `temporary-persistence`: The `--fly` execution mode with an isolated, self-deleting data directory.

### Modified Capabilities
- `command-interface`: the command file is the first argument that is not an option, so `--fly` can precede it; interactive mode applies when no file is given.
- `project-build`: the executable JAR is produced as `target/parque.jar`.

## Impact

- `App` gains option parsing and data-directory selection; new helper class for the temporary directory; `pom.xml` `finalName`; `.gitignore` ignores `tmp/`.
