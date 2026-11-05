# Tasks

## 1. Interactive session

- [x] 1.1 Add `InteractiveSession` that reads lines from an injected `BufferedReader` until end of input; verify with a unit test that it consumes all lines and returns normally on end of input
- [x] 1.2 Make `App.main` start an `InteractiveSession` on `System.in` when no arguments are given; verify `AppTest` runs `main` with an empty standard input and it returns normally

## 2. Executable JAR packaging

- [x] 2.1 Configure `maven-jar-plugin` with `mainClass` `es.upm.amusementpark.App` and `finalName` `amusement-park` in `pom.xml`; verify `mvn package` succeeds and creates `target/amusement-park.jar`
- [x] 2.2 Verify the JAR manifest contains `Main-Class: es.upm.amusementpark.App` (`unzip -p target/amusement-park.jar META-INF/MANIFEST.MF`)

## 3. Integration

- [x] 3.1 Run `java -jar target/amusement-park.jar` with closed standard input (`< /dev/null`) and verify exit status 0; run it with a held-open stdin and verify it keeps waiting
- [x] 3.2 Run `mvn clean package` and verify all tests (including the project-initialization baseline) pass
