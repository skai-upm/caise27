# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `mvn archetype:generate` (`maven-archetype-quickstart`, groupId `es.upm.parque`, artifactId `parque`, package `es.upm.parque`) in a temporary directory and move `pom.xml` and `src/` to the repository root; verify `pom.xml`, `src/main/java/es/upm/parque/App.java` and `src/test/java/es/upm/parque/AppTest.java` exist

## 2. Build configuration

- [x] 2.1 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` compiles and passes the archetype test
- [x] 2.2 Set `finalName` to `parque` and configure the `maven-jar-plugin` manifest `mainClass` to `es.upm.parque.App`; verify `mvn package` builds `target/parque.jar` and `java -jar target/parque.jar` prints the greeting and exits with status 0
- [x] 2.3 Verify no domain functionality was introduced: `src/main/java` only contains `App.java`
