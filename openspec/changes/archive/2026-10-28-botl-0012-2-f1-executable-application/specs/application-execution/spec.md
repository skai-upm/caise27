## MODIFIED Requirements

### Requirement: Executable JAR built with Maven
Running `mvn package` on a clean checkout SHALL produce the JAR `target/amusement-park.jar`, whose manifest defines the application entry point, so that it can be launched with `java -jar` without an IDE, extra classpath or configuration.

#### Scenario: Build and launch from scratch
- **WHEN** `mvn package` is executed on a clean checkout and the produced JAR is launched with `java -jar target/amusement-park.jar` and input `EXIT`
- **THEN** the application starts, processes `EXIT` and terminates normally

#### Scenario: Entry point defined
- **WHEN** the manifest of `target/amusement-park.jar` is inspected
- **THEN** its `Main-Class` is the application entry point class, which declares `public static void main(String[])`
