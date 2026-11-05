## MODIFIED Requirements

### Requirement: Executable JAR built by Maven
Running `mvn package` on the project SHALL produce the JAR file `target/amusement-park.jar`, whose manifest declares the application entry-point class and which contains the runtime dependencies (jBCrypt), so that it runs with `java -jar` alone.

#### Scenario: Package produces the executable JAR
- **WHEN** `mvn package` is executed on a clean checkout
- **THEN** the build finishes with `BUILD SUCCESS`
- **AND** `target/amusement-park.jar` exists
- **AND** its manifest contains a `Main-Class` attribute naming the application entry point
- **AND** it contains the `org/mindrot/jbcrypt/BCrypt` class

#### Scenario: Failed build produces no executable JAR claim
- **WHEN** the sources do not compile
- **THEN** `mvn package` fails and does not report `BUILD SUCCESS`
