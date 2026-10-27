# Tasks

## 1. Executable JAR configuration

- [x] 1.1 Add `<finalName>${project.artifactId}</finalName>` and a maven-jar-plugin manifest with `mainClass` `es.upm.amusementpark.App` to `pom.xml`; verify `mvn package` succeeds and `target/amusement-park.jar` exists with `Main-Class: es.upm.amusementpark.App` in its manifest

## 2. Execution verification

- [x] 2.1 Verify interactive mode: `printf 'HELP\nEXIT\n' | java -jar target/amusement-park.jar` prints the help text and exits normally
- [x] 2.2 Verify file mode: `java -jar target/amusement-park.jar input.txt` with a temporary file containing `HELP` and `EXIT` prints the help text and exits normally
