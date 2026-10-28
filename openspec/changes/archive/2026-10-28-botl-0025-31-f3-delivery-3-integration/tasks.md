# Tasks

## 1. Integration verification

- [x] 1.1 Add `Delivery3IntegrationTest` (full mixed command file, restart check, normal vs `--fly` comparison and cleanup); verify it passes
- [x] 1.2 Add `Delivery3HelpTest` and `Delivery3RestrictionsTest`; verify they pass

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and `java -jar target/amusement-park.jar input_fase3.txt` and `--fly input_fase3.txt` in a scratch directory; verify outputs and that `tmp/` is empty
- [x] 2.2 Commit with the exact message `[F3-C10] Complete delivery 3 integration`; verify with `git log -1 --format=%s`
