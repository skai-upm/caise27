# Tasks

## 1. Maven project scaffolding

- [x] 1.1 Create `pom.xml` with quickstart coordinates, `maven.compiler.release=21`, JUnit Jupiter test dependencies and pinned plugin versions; verify with `mvn -q help:evaluate -Dexpression=maven.compiler.release -DforceStdout` printing `21`
- [x] 1.2 Create `src/main/java/es/upm/amusementpark/App.java` and `src/test/java/es/upm/amusementpark/AppTest.java` as in the archetype; verify `mvn package` reports `BUILD SUCCESS` with the test passing

## 2. Milestone commit

- [x] 2.1 Commit `pom.xml` and the project structure with the exact message `[F0-C01] Initialize Maven project with JDK 21`; verify with `git log -1 --format=%s`
