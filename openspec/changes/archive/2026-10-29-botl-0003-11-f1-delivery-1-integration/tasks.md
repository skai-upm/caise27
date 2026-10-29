# Tasks

## 1. Executable JAR

- [x] 1.1 Set `<finalName>amusement-park</finalName>` and configure `maven-jar-plugin` with `mainClass` `es.upm.amusementpark.App`; verify `mvn clean package` succeeds and `unzip -p target/amusement-park.jar META-INF/MANIFEST.MF` shows `Main-Class: es.upm.amusementpark.App`
- [x] 1.2 Verify every dependency in `pom.xml` is test-scoped (`mvn dependency:list -DincludeScope=runtime` lists none)

## 2. Command parsing and robustness

- [x] 2.1 Parse lines with `split(";", -1)` taking the first field as keyword; reject HELP/EXIT with extra fields; verify with unit tests for `HELP;extra`, `EXIT;x` and `  HELP  `
- [x] 2.2 Make `run` catch unexpected runtime errors per line, print an error and continue; verify with a unit test using a processor whose handler throws, asserting the next line is still processed
- [x] 2.3 Verify the nonexistent command file case prints an error naming the file and no stack trace (existing `AppTest` extended to assert the file name)

## 3. Integration verification

- [x] 3.1 Run `mvn clean package`, then `printf 'HELP\nEXIT\n' | java -jar target/amusement-park.jar` and `java -jar target/amusement-park.jar input.txt` with a file containing `HELP`, `FOO`, `EXIT`, `HELP`; verify help is shown, `FOO` is rejected, and processing stops at EXIT in both modes
