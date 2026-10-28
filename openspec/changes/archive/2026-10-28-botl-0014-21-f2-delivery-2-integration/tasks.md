# Tasks

## 1. HELP

- [x] 1.1 Replace the HELP listing with the Delivery 2 minimum syntax (source §15); verify with the HELP unit test

## 2. Integration verification

- [x] 2.1 Add `ModelStructureTest` for relationship directions, ticket abstraction and single controller instance; verify it passes
- [x] 2.2 Add `Delivery2IntegrationTest` running full command files in normal mode (with stored users, restart check) and in `--fly` mode (output equality with normal mode, no temporary data left); verify it passes

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run `java -jar target/amusement-park.jar input_fase2.txt` and `--fly input_fase2.txt` with a Delivery 2 command file in a scratch directory; verify outputs and that `tmp/` is empty
- [x] 3.2 Commit with the exact message `[F2-C10] Complete delivery 2 integration`; verify with `git log -1 --format=%s`
