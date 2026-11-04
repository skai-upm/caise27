# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.park`, artifactId `amusement-park`) and move it to the repository root; verify `pom.xml`, `src/main/java/es/upm/park/App.java` and `src/test/java/es/upm/park/AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml`; verify `mvn test` compiles and passes, and `target/classes/es/upm/park/App.class` has major version 65

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` manifest with `mainClass` `es.upm.park.App`; verify `mvn package` succeeds and `java -jar target/amusement-park-1.0-SNAPSHOT.jar` exits 0 without a manifest error

## 3. Verification

- [x] 3.1 Review production sources to confirm only the entry point exists with no domain functionality, and run `mvn clean test` for a final clean build
