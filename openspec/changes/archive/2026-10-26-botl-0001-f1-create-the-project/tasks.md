# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `parque`, package `es.upm.parque`) and place `pom.xml` and `src/` at the repository root; verify `src/main/java/es/upm/parque/App.java` and `src/test/java/es/upm/parque/AppTest.java` exist
- [x] 1.2 Set the compiler to Java 21 (`maven.compiler.release=21`) in `pom.xml` and verify `mvn compile` succeeds

## 2. Build and packaging

- [x] 2.1 Configure `maven-jar-plugin` with `mainClass` `es.upm.parque.App` and `finalName` `parque`; verify `mvn package` produces `target/parque.jar` and `java -jar target/parque.jar` runs and exits normally
- [x] 2.2 Verify `mvn test` succeeds with the archetype's test passing
