# Tasks

## 1. Executable JAR and execution modes

- [x] 1.1 Configure `maven-jar-plugin` with `Main-Class: es.upm.etsisi.amusementpark.App`; verify `unzip -p target/*.jar META-INF/MANIFEST.MF` shows the entry after `mvn package`
- [x] 1.2 Make `App.main` use standard input without arguments and the file in `args[0]` otherwise, reporting an unreadable/missing file with an error message; verify with integration tests for both modes and for a missing file

## 2. Controlled error handling

- [x] 2.1 Catch unexpected runtime failures of a command in `CommandProcessor`, report them and continue with the next line; verify with a unit test registering a failing command followed by `HELP`
- [x] 2.2 Remove regular-expression usage from the test code; verify with `grep -rn "Pattern\|regex\|matches(" src` returning nothing

## 3. Delivery verification

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run `java -jar target/amusement-park-1.0-SNAPSHOT.jar` interactively with `HELP`/`EXIT` and with a `.txt` command file; verify the outputs match the specs
- [x] 3.2 Commit with the exact message `[F1-C09] Complete delivery 1 integration`; verify with `git log -1 --format=%s`
