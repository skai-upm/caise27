# Tasks

## 1. Executable packaging

- [x] 1.1 Set `<finalName>amusement-park</finalName>` in `pom.xml` and verify `mvn package` produces `target/amusement-park.jar`
- [x] 1.2 Configure `maven-jar-plugin` manifest `mainClass` = `es.upm.parque.App` and verify with `unzip -p target/amusement-park.jar META-INF/MANIFEST.MF` that `Main-Class: es.upm.parque.App` is present
- [x] 1.3 Add a unit test asserting `App.main(new String[0])` runs without throwing, and verify it passes in `mvn test`

## 2. Launch verification

- [x] 2.1 Run `java -jar target/amusement-park.jar < /dev/null` and verify exit status 0 with no manifest error
- [x] 2.2 Copy the JAR to a temporary directory, launch it there with `java -jar`, and verify it starts (exit status 0)
- [x] 2.3 Regression: verify `mvn verify` still succeeds and compiled classes remain major version 65 (project-initialization requirements intact)
