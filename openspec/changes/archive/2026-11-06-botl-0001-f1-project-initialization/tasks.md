# Tasks

## 1. Maven project generation

- [x] 1.1 Generate a project with `mvn archetype:generate` (maven-archetype-quickstart, groupId `es.upm.park`, artifactId `amusement-park`) in a temporary directory, copy `pom.xml` and `src/` to the repository root, and verify that `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `maven.compiler.release` = 21 and UTF-8 source encoding in `pom.xml`, and verify with `mvn help:effective-pom` (or by inspecting the compiler configuration) that the release is 21
- [x] 1.3 Make sure the test dependency is JUnit 5 and the surefire plugin can run it, and verify that `mvn -q test` runs the archetype test and passes

## 2. Verification

- [x] 2.1 Run `mvn -q clean test` from a clean state and verify that the build succeeds, and verify with `git status` that `target/` is ignored
- [x] 2.2 Update `README.md` with build prerequisites (JDK 21+, Maven) and the build command, and verify that the documented command succeeds
