# Tasks

## 1. Robustness

- [x] 1.1 Add a catch-all for unexpected runtime failures in `CommandProcessor.process`; verify with a `CommandProcessorTest` case using a repository whose lookup throws, followed by a normal command

## 2. Integration verification and docs

- [x] 2.1 Add `IntegrationTest` reproducing the §16 example through stdin and through `input.txt` (identical outputs, final users file) plus the mixed invalid-input scenario; verify it passes
- [x] 2.2 Add `TechnicalRestrictionsTest` scanning production sources for forbidden imports/regex and class-responsibility separation; verify it passes
- [x] 2.3 Document build (`mvn package`) and run modes in `README.md`; verify by building from scratch (`mvn clean package`) and running `java -jar target/nombre-aplicacion.jar input.txt` with the §16 example

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F1-C09] Complete delivery 1 integration`; verify with `git log -1 --format=%s`
