# Tasks

## 1. Build configuration

- [x] 1.1 Set `finalName` to `amusement-park` in `pom.xml`; verify `mvn clean package` produces `target/amusement-park.jar` whose manifest has `Main-Class: es.upm.etsisi.amusementpark.App`
- [x] 1.2 Add `EntryPointTest` checking the configured main class and its `main` method; verify it passes in `mvn package`

## 2. Delivery

- [x] 2.1 Run `java -jar target/amusement-park.jar` interactively (`HELP`, `EXIT`) and with an `input.txt` command file; verify the outputs
- [x] 2.2 Commit with the exact message `[F0-C02] Configure executable JAR`; verify with `git log -1 --format=%s`
