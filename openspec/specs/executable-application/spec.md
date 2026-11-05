# executable-application Specification

## Purpose

Defines how the Amusement Park application is packaged by Maven as an executable JAR and launched from a terminal independently of any IDE.

## Requirements

### Requirement: Executable JAR packaging
Running `mvn package` SHALL produce the file `target/amusement-park.jar`, and its manifest SHALL declare the application entry-point class as `Main-Class`, so that it can be launched with `java -jar target/amusement-park.jar` without an IDE or additional classpath configuration.

#### Scenario: Package produces an executable JAR
- **GIVEN** the Maven project in a clean checkout
- **WHEN** `mvn package` is executed
- **THEN** the build succeeds and `target/amusement-park.jar` exists
- **AND** its manifest contains a `Main-Class` attribute naming the entry-point class

#### Scenario: JAR starts outside the IDE
- **GIVEN** the packaged `target/amusement-park.jar`
- **WHEN** it is launched from a terminal with `java -jar target/amusement-park.jar`
- **THEN** the application starts without `no main manifest attribute` or class-loading errors

#### Scenario: Failing build produces no new executable JAR
- **GIVEN** sources that do not compile
- **WHEN** `mvn package` is executed
- **THEN** the build fails and no executable JAR is produced by that build

### Requirement: Interactive mode on standard input
When launched without arguments, the application SHALL start in interactive mode, waiting for commands read line by line from standard input, and SHALL terminate normally (exit status 0) when standard input reaches its end.

#### Scenario: Application waits for input
- **GIVEN** the application launched with `java -jar target/amusement-park.jar` and no arguments
- **WHEN** no input has been provided yet
- **THEN** the process remains running, waiting to read from standard input

#### Scenario: End of input terminates normally
- **GIVEN** the application running in interactive mode
- **WHEN** standard input is closed (end of input)
- **THEN** the application terminates with exit status 0
