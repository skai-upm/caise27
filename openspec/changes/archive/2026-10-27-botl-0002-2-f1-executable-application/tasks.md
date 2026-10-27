# Tasks

## 1. Entry point and input modes

- [x] 1.1 Create `Main` with `run(args, in, err)` reading lines from standard input (no args) or from the file given as argument, reporting unreadable files on stderr with exit code 1; remove the archetype `App`/`AppTest`; verify with `MainTest` covering stdin mode, file mode and missing file
- [x] 1.2 Configure `pom.xml` with `<finalName>nombre-aplicacion</finalName>` and the `maven-jar-plugin` manifest `mainClass`; verify `mvn package` succeeds, `target/nombre-aplicacion.jar` contains `Main-Class`, and `java -jar target/nombre-aplicacion.jar < /dev/null` and `java -jar target/nombre-aplicacion.jar input.txt` exit with status 0

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F0-C02] Configure executable JAR`; verify with `git log -1 --format=%s`
