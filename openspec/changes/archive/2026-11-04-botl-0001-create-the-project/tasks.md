# Tasks

## 1. Generate the skeleton

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.etsisi`, artifactId `amusement-park`, package `es.upm.etsisi.amusementpark`) and place `pom.xml` and `src/` at the repository root; verify `pom.xml`, `src/main/java/.../App.java` and `src/test/java/.../AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml`; verify `mvn test` passes and `javap -v` on `App.class` reports major version 65

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` with manifest `mainClass` `es.upm.etsisi.amusementpark.App`; verify `mvn package` succeeds and `java -jar target/amusement-park-*.jar` runs without a missing-manifest error

## 3. Integration checks

- [x] 3.1 Run `mvn clean test` and confirm at least one test runs with zero failures, and confirm production sources contain only `App` (no domain code)
