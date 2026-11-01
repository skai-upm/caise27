# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
The build SHALL produce an executable JAR whose manifest entry point starts the application and which bundles its runtime dependencies, so it can be launched with `java -jar` without an IDE or extra classpath.

#### Scenario: Launch packaged application
- **GIVEN** the project has been packaged with `mvn package`
- **WHEN** the produced JAR is launched with `java -jar`
- **THEN** the application starts and enters its command-execution path

## REMOVED Requirements

### Requirement: Runtime dependencies limited to the standard library
**Reason**: Delivery 3 requires an external BCrypt library declared through Maven.
**Migration**: Replaced by "Runtime dependencies limited to BCrypt" below.

## ADDED Requirements

### Requirement: Runtime dependencies limited to BCrypt
Besides the Java standard library, the only runtime dependency SHALL be a BCrypt password-hashing library declared in `pom.xml`; test-scope testing libraries are permitted.

#### Scenario: Declared dependencies
- **WHEN** the build's dependencies are inspected
- **THEN** the only non-test dependency is the BCrypt library
