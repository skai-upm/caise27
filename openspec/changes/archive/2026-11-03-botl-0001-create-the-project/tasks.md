# Tasks

## 1. Generate the project

- [x] 1.1 Generate the project with `mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DgroupId=es.upm.etsisi -DartifactId=amusement-park -Dpackage=es.upm.etsisi.amusementpark` in a temporary directory and move its contents to the repository root; verify `pom.xml`, `src/main/java/es/upm/etsisi/amusementpark/App.java` and `src/test/java/es/upm/etsisi/amusementpark/AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` compiles and passes the archetype test

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` with manifest `mainClass` `es.upm.etsisi.amusementpark.App`; verify `mvn package` succeeds and `java -jar target/amusement-park-1.0-SNAPSHOT.jar` runs without a missing-manifest error

## 3. Verification

- [x] 3.1 Review `src/main/java` and verify it only contains the entry point (no domain functionality) and that `mvn clean test` passes from a clean state
