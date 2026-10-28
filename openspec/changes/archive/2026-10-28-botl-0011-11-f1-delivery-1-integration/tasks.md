# Tasks

## 1. Robustness

- [x] 1.1 Contain unexpected runtime failures in `CommandProcessor.process` as `ERROR:` responses; verify a unit test where the service throws a `RuntimeException` and the next command still works

## 2. Integration verification

- [x] 2.1 Add an integration test running the statement's full session from a file and from stdin; verify outputs and that only Ana (new phone and email) is stored
- [x] 2.2 Audit technical restrictions: dependencies test-only, production imports only `java.*`/project packages, no regex APIs, separated user/main/persistence classes; verify by `grep` inspection with no findings
- [x] 2.3 Run `mvn clean package` and the JAR from a clean temporary directory with the statement's `input.txt` and interactively; verify `BUILD SUCCESS` and expected outputs

## 3. Documentation and repository

- [x] 3.1 Write `README.md` (build, interactive and file execution, data file, command syntax) and add `users.txt` to `.gitignore`; verify the documented commands run as written
- [x] 3.2 Commit with the exact message `[F1-C09] Complete delivery 1 integration` and check that the eleven roadmap commits appear in order with `git log --reverse --format=%s`
