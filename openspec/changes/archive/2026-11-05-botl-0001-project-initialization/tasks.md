# Tasks

## 1. Maven project scaffolding

- [x] 1.1 Create `pom.xml` at the repository root with quickstart coordinates, `maven.compiler.release=21`, UTF-8 encoding, JUnit Jupiter test dependency and pinned compiler/surefire plugins; verify `mvn -q validate` succeeds
- [x] 1.2 Create `src/main/java/es/upm/amusementpark/App.java` (archetype placeholder) and verify the file exists in the standard layout
- [x] 1.3 Create `src/test/java/es/upm/amusementpark/AppTest.java` with a passing JUnit test and verify the file exists in the standard layout

## 2. Build verification

- [x] 2.1 Run `mvn clean test` and verify `BUILD SUCCESS` with the test executed
- [x] 2.2 Inspect `target/classes/es/upm/amusementpark/App.class` with `javap -v` and verify the major version is 65 (Java 21)
- [x] 2.3 Verify `target/` is ignored by `.gitignore` so build output is not committed
