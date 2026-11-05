# executable-application Specification

## Purpose
Defines how the Amusement Park application is packaged and started: `mvn package` builds an executable JAR that runs from a terminal with `java -jar`, independent of any IDE.

## Requirements

### Requirement: Executable JAR built by Maven
Running `mvn package` on the project SHALL produce the JAR file `target/amusement-park.jar`, whose manifest declares the application entry-point class.

#### Scenario: Package produces the executable JAR
- **WHEN** `mvn package` is executed on a clean checkout
- **THEN** the build finishes with `BUILD SUCCESS`
- **AND** `target/amusement-park.jar` exists
- **AND** its manifest contains a `Main-Class` attribute naming the application entry point

#### Scenario: Failed build produces no executable JAR claim
- **WHEN** the sources do not compile
- **THEN** `mvn package` fails and does not report `BUILD SUCCESS`

### Requirement: Application starts from the terminal
The JAR produced by `mvn package` SHALL start the application when launched with `java -jar target/amusement-park.jar`, without requiring an IDE or an explicit classpath or main-class argument.

#### Scenario: Launch with java -jar
- **WHEN** `java -jar target/amusement-park.jar` is executed from a terminal with empty standard input
- **THEN** the application's entry point runs
- **AND** the process exits with status 0 and no "no main manifest attribute" error

#### Scenario: Launch without IDE classpath
- **WHEN** the JAR is copied away from the project and launched with `java -jar`
- **THEN** the application still starts, because the JAR has no external runtime dependencies
