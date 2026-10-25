# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR with a main class
The project SHALL include an executable main class, and `mvn package` SHALL produce a JAR whose manifest declares that class as `Main-Class` and that contains the runtime dependencies, so it can be launched with `java -jar` without extra classpath configuration.

#### Scenario: Running the packaged JAR
- **WHEN** `mvn package` is run and the resulting JAR is launched with `java -jar`
- **THEN** the main class runs and the process exits normally

#### Scenario: Dependencies available at runtime
- **WHEN** the packaged JAR is launched with `java -jar` and a user is created
- **THEN** the password is hashed with the bundled BCrypt library without class-loading errors
