# Tasks

## 1. Entry point

- [x] 1.1 Replace archetype `App` with entry-point class `es.upm.park.Main` (prints a startup banner, accepts optional arguments) and migrate its unit test; verify `mvn test` passes

## 2. Executable packaging

- [x] 2.1 Configure `maven-jar-plugin` manifest `mainClass` = `es.upm.park.Main` and `finalName` = `amusement-park` in `pom.xml`; verify `mvn package` succeeds and `target/amusement-park.jar` contains `Main-Class: es.upm.park.Main`
- [x] 2.2 Run `java -jar target/amusement-park.jar` and `java -jar target/amusement-park.jar input.txt` from the terminal; verify the banner is printed and the exit code is 0
- [x] 2.3 Document `mvn package` and the `java -jar` commands in `README.md`; verify the documented commands run as written

## 3. Regression

- [x] 3.1 Verify `project-initialization` behavior still holds: compiler release 21 and `mvn compile` BUILD SUCCESS
