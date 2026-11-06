# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR built with Maven
The project SHALL produce, with `mvn package` from a clean checkout, an executable JAR at `target/amusement-park.jar` that starts the application with `java -jar` and needs no IDE, extra classpath or other configuration. Its only third-party runtime library SHALL be the BCrypt library used for password protection, bundled inside the JAR.

#### Scenario: Package and run the JAR
- **GIVEN** a clean checkout with JDK 21+ and Maven
- **WHEN** `mvn package` is run and then `java -jar target/amusement-park.jar` is started with the input `EXIT`
- **THEN** the build succeeds, the application starts, and it terminates normally

#### Scenario: Runtime uses only the Java standard library
- **GIVEN** the project's dependency declarations
- **WHEN** the runtime classpath is resolved
- **THEN** it contains no third-party libraries other than `org.mindrot:jbcrypt` (test-only dependencies are allowed)

#### Scenario: BCrypt bundled in the JAR
- **WHEN** the contents of `target/amusement-park.jar` are listed
- **THEN** they include the `org/mindrot/jbcrypt/BCrypt.class` class
