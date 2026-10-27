# Tasks

## 1. Maven project generation

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, package `es.upm.amusementpark`) and move it to the repository root; verify `pom.xml`, `src/main/java/es/upm/amusementpark/App.java` and `src/test/java/es/upm/amusementpark/AppTest.java` exist
- [x] 1.2 Configure `maven.compiler.release` to `21` in `pom.xml`; verify with `mvn help:effective-pom` (or by inspecting the pom) that the release is 21

## 2. Build verification

- [x] 2.1 Run `mvn package` at the repository root and verify the build succeeds with the unit test passing
