# Tasks

## 1. Maven project scaffolding

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, package `es.upm.amusementpark`) and place `pom.xml` and `src/` at the repository root; verify `pom.xml`, `src/main/java/es/upm/amusementpark/App.java` and `src/test/java/es/upm/amusementpark/AppTest.java` exist
- [x] 1.2 Configure `maven.compiler.release` to 21 in `pom.xml`; verify the property is present and `mvn -q compile` succeeds

## 2. Build verification

- [x] 2.1 Run `mvn package` from the repository root and verify `BUILD SUCCESS` with the sample test passing
- [x] 2.2 Verify the compiled `App.class` has class-file major version 65 (Java 21) using `javap -v`
