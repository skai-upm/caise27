# Spec Delta

## Purpose

Defines how the Amusement Park application is packaged and launched: Maven builds a self-contained executable JAR that starts from the terminal with `java -jar`, independently of any IDE.

## ADDED Requirements

### Requirement: Executable JAR produced by Maven
Running `mvn package` SHALL produce the executable JAR `target/amusement-park.jar`, whose manifest declares the application's entry-point class.

#### Scenario: Packaging produces the JAR
- **GIVEN** the Maven project
- **WHEN** `mvn package` is executed
- **THEN** the build succeeds and `target/amusement-park.jar` exists
- **AND** its manifest contains a `Main-Class` attribute naming the application entry point

#### Scenario: Packaging fails on compilation errors
- **GIVEN** sources that do not compile
- **WHEN** `mvn package` is executed
- **THEN** the build fails and no new executable JAR is produced

### Requirement: Launch with java -jar
The packaged JAR MUST start the application when launched with `java -jar target/amusement-park.jar`, without IDE support or an explicit classpath.

#### Scenario: Application starts from the terminal
- **GIVEN** the JAR produced by `mvn package`
- **WHEN** `java -jar target/amusement-park.jar` is run from a terminal
- **THEN** the application's entry point runs and the process does not fail with "no main manifest attribute" or a class-loading error

#### Scenario: Launch with a file argument does not fail
- **GIVEN** the JAR produced by `mvn package`
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the application's entry point runs and receives `input.txt` as its argument
