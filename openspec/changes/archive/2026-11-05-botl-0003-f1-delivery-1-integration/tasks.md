# Tasks

## 1. Executable packaging

- [x] 1.1 Configure `finalName` `amusement-park` and the `maven-jar-plugin` manifest main class in `pom.xml`; verify `mvn -q package` creates `target/amusement-park.jar` with `Main-Class` in its manifest

## 2. Controlled failure handling

- [x] 2.1 Contain unexpected exceptions per command in `CommandProcessor` and verify with a unit test registering a failing handler followed by `HELP`

## 3. Technical restrictions

- [x] 3.1 Add a unit test verifying all `pom.xml` dependencies are test-scoped and `src/main/java` uses no regex/encryption APIs; verify it passes

## 4. Integration verification

- [x] 4.1 After `mvn package`, run `java -jar target/amusement-park.jar` with `HELP`/`EXIT` on stdin, with an `input.txt` file, and with a nonexistent file; verify outputs and exit status 0 in all cases
