# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `mvn archetype:generate` using `org.apache.maven.archetypes:maven-archetype-quickstart:1.5` (groupId `es.upm.parque`, artifactId `parque`, package `es.upm.parque`) and move it to the repository root; verify `pom.xml`, `src/main/java/es/upm/parque/App.java` and `src/test/java/es/upm/parque/AppTest.java` exist

## 2. Build configuration

- [x] 2.1 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` compiles and runs the archetype test successfully
- [x] 2.2 Configure `<finalName>parque</finalName>` and the `maven-jar-plugin` manifest `mainClass` (`es.upm.parque.App`); verify `mvn package` then `java -jar target/parque.jar` runs and exits with status 0
- [x] 2.3 Verify the generated main class contains no domain functionality (entry point only) and that compiled classes target Java 21 (class file major version 65)
