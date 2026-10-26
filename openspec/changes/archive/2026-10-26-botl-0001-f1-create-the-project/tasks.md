# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `mvn archetype:generate` (maven-archetype-quickstart 1.5, groupId `es.upm.parque`, artifactId `parque`) and move it to the repository root; verify `pom.xml`, `src/main/java` and `src/test/java` exist at the root
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml`; verify `mvn compile` succeeds

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` manifest with the main class and `finalName` `parque`; verify `mvn package` produces `target/parque.jar` and `java -jar target/parque.jar` exits 0
- [x] 2.2 Run `mvn test` and verify the archetype test executes and passes with no domain code added
