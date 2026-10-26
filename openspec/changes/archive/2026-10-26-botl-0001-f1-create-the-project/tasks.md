# Tasks

## 1. Project scaffolding

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `parque`) and place `pom.xml` and `src/` at the repository root; verify the standard `src/main/java` and `src/test/java` trees exist
- [x] 1.2 Configure `pom.xml` with `maven.compiler.release=21` and UTF-8 encoding; verify `mvn compile` succeeds

## 2. Executable entry point and tests

- [x] 2.1 Keep the archetype `App` main class as a minimal entry point printing a start-up message; verify it contains no domain logic
- [x] 2.2 Configure the `maven-jar-plugin` manifest `mainClass`; verify `mvn package` then `java -jar target/parque-1.0-SNAPSHOT.jar` runs and exits normally
- [x] 2.3 Keep the archetype unit test structure; verify `mvn test` passes
