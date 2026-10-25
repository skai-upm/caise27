## MODIFIED Requirements

### Requirement: Java 21 compilation
The project SHALL compile targeting the Java 21 language level and SHALL use only the Java standard library at runtime, plus the BCrypt library declared in `pom.xml` for password hashing.

#### Scenario: Build targets Java 21
- **WHEN** `mvn test` is executed
- **THEN** sources are compiled with Java release 21 and the build succeeds

#### Scenario: No runtime dependencies
- **WHEN** the declared dependencies are inspected
- **THEN** apart from test-scoped dependencies only the BCrypt library is present

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and that contains its runtime dependencies, so it can be launched with `java -jar`.

#### Scenario: Launch packaged JAR
- **WHEN** `mvn package` is executed and the resulting JAR is run with `java -jar`
- **THEN** the application's main class starts without specifying it on the command line and password hashing works
