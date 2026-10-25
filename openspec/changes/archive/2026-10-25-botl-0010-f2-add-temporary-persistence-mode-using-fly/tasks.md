# Tasks

## 1. Temporary directory

- [x] 1.1 Create `TemporaryDirectory` (`create` with timestamp name and numeric suffix on collision, `deleteRecursively` idempotent); verify with `TemporaryDirectoryTest` using `@TempDir`
- [x] 1.2 Add `tmp/` to `.gitignore`; verify `git check-ignore tmp/x`

## 2. Fly mode

- [x] 2.1 Add `App.launch` (option parsing, temporary or permanent data directory, cleanup in `finally`, creation error) and the shutdown hook in `main`; verify with `AppTest` (fly interactive and file modes, permanent data untouched, directory deleted after EXIT and after file errors, normal mode unchanged)
- [x] 2.2 Verify end to end with `mvn package`: `java -jar target/parque.jar --fly in.txt` and an interactive `--fly` run in a scratch directory that has a `data/users.txt`, checking that `data/` is unchanged and `tmp/` holds no execution directory afterwards
