# Spec Delta

## ADDED Requirements

### Requirement: Executable JAR
Running `mvn package` SHALL produce the file `target/nombre-aplicacion.jar`, whose manifest declares the application entry point so it can be launched with `java -jar target/nombre-aplicacion.jar`.

#### Scenario: Build produces runnable JAR
- **WHEN** `mvn package` is executed
- **THEN** `target/nombre-aplicacion.jar` exists and its manifest contains a `Main-Class` attribute naming the entry point class

#### Scenario: JAR starts outside the IDE
- **WHEN** `java -jar target/nombre-aplicacion.jar` is executed from a terminal with empty standard input
- **THEN** the application starts without class-loading errors and terminates with exit status 0
