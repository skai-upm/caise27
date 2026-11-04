# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR packaging
Packaging the project SHALL produce a JAR whose manifest declares the main class and that contains the runtime dependencies, so it can be launched with `java -jar`.

#### Scenario: Packaged JAR starts
- **GIVEN** a successful `mvn package`
- **WHEN** `java -jar` is run on the produced JAR
- **THEN** the application starts without a "no main manifest attribute" error and exits with status 0

#### Scenario: Packaged with runtime dependencies
- **GIVEN** the project declares a runtime library dependency
- **WHEN** `java -jar` is run on the JAR produced by `mvn package`
- **THEN** the application starts without missing-class errors because the runtime dependencies are included in the JAR
