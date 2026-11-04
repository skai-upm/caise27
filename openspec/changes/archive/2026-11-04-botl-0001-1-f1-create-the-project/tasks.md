# Tasks

## 1. Project generation

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.park`, artifactId `amusement-park`) and place `pom.xml` and `src/` at the repository root; verify `pom.xml`, `src/main/java/es/upm/park/App.java` and `src/test/java/es/upm/park/AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` compiles and passes with at least one executed test

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` manifest `mainClass` to `es.upm.park.App` and verify `mvn package` then `java -jar target/amusement-park-*.jar` exits with status 0 and no missing-manifest error
- [x] 2.2 Verify the class files in the JAR have major version 65 (Java 21) and that production sources contain only the entry-point class (no domain types)
