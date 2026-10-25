# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR
Packaging the project SHALL produce a JAR whose manifest declares the application's main class and which contains its runtime dependencies, so it can be run with `java -jar` without any additional classpath.

#### Scenario: Run packaged application
- **WHEN** `mvn package` is executed and the resulting JAR is run with `java -jar`
- **THEN** the application's main class starts without specifying a class name on the command line

#### Scenario: Dependencies bundled
- **WHEN** `java -jar target/parque.jar` executes a command that hashes a password
- **THEN** it succeeds without any extra classpath entries

## REMOVED Requirements

### Requirement: Standard library only at runtime
**Reason**: Delivery 3 requires adding an external library through Maven to protect passwords with BCrypt.
**Migration**: Runtime dependencies are now governed by the "Declared runtime dependencies" requirement; BCrypt is declared in `pom.xml`.

## ADDED Requirements

### Requirement: Declared runtime dependencies
Runtime dependencies SHALL be declared in `pom.xml`; the only runtime dependency beyond the Java standard library SHALL be a BCrypt implementation. Test-scoped dependencies remain allowed.

#### Scenario: BCrypt declared
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** a BCrypt library is declared with compile scope and every other dependency has `test` scope
