# Tasks

## 1. Generate the Quickstart project

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `com.parque`, artifactId `parque`, package `com.parque`) and place `pom.xml` and `src/` at the repository root; verify `src/main/java/com/parque/App.java` and `src/test/java/com/parque/AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 and UTF-8 source encoding in `pom.xml`; verify `mvn test` reports BUILD SUCCESS with 0 failures and the compiled `App.class` has major version 65

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` manifest `mainClass` to `com.parque.App`; verify `mvn package` succeeds, `META-INF/MANIFEST.MF` contains `Main-Class: com.parque.App`, and `java -jar target/parque-1.0-SNAPSHOT.jar` exits with status 0

## 3. Integration check

- [x] 3.1 Verify no domain functionality was added (only `App` in production sources) and `openspec validate botl-0001-create-the-project` passes
