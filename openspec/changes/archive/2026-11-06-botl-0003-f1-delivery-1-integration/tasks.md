# Tasks

## 1. Executable packaging

- [x] 1.1 Configure `finalName` `amusement-park` and the `maven-jar-plugin` manifest `mainClass` `es.upm.park.App` in `pom.xml`, and verify that `mvn -q clean package` creates `target/amusement-park.jar` and that `unzip -p target/amusement-park.jar META-INF/MANIFEST.MF` shows the Main-Class
- [x] 1.2 Verify that the runtime classpath has no third-party libraries (`mvn dependency:list` shows only test-scope dependencies) and that the main sources use no `java.util.regex`, `matches(`/`replaceAll(` or crypto APIs

## 2. Robust command loop

- [x] 2.1 Make `CommandRunner` ignore blank or whitespace-only lines silently, and verify with a `CommandRunnerTest` case that a file with blank lines between HELP and EXIT produces no error output
- [x] 2.2 Make `CommandRunner` report a `RuntimeException` thrown while processing a line as `ERROR: Could not process command: <line>` and continue, and verify with a test using a failing processor that the next line is still processed

## 3. Integration verification

- [x] 3.1 Build with `mvn clean package` and run `java -jar target/amusement-park.jar input.txt` (FOO, blank line, HELP, EXIT, HELP) and with piped stdin (HELP, EXIT), and verify the output order, that nothing runs after EXIT, and that a nonexistent file path gives a controlled error message
- [x] 3.2 Document `mvn package` and both `java -jar` modes in `README.md`, and verify that the documented commands work as written
