# Tasks

## 1. Executable JAR build

- [x] 1.1 Configure `maven-jar-plugin` with manifest `mainClass` `es.upm.amusementpark.App` and set `finalName` to `amusement-park`; verify `mvn package` creates `target/amusement-park.jar` and its manifest contains `Main-Class: es.upm.amusementpark.App`

## 2. Execution modes

- [x] 2.1 Implement the shared line-reading loop and the entry point selecting standard input (no arguments) or the given file (one argument), reporting unreadable files on stderr with exit status 1; verify with unit tests for interactive input, file input and missing file
- [x] 2.2 Run `java -jar target/amusement-park.jar` with empty stdin and with a sample `input.txt`, and with a missing file; verify exit statuses 0, 0 and 1

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F0-C02] Configure executable JAR`; verify with `git log -1 --format=%s`
