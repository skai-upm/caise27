# Spec Delta

## MODIFIED Requirements

### Requirement: Standard library only
The production code SHALL use only the Java standard library plus the BCrypt library declared for password hashing; no other runtime dependencies, frameworks or databases SHALL be declared.

#### Scenario: No runtime dependencies
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** the only non-test dependency is the BCrypt library

### Requirement: Executable JAR
`mvn package` SHALL produce a JAR whose manifest declares the application main class so it can be launched with `java -jar`, with its runtime dependencies available to it.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is run and then `java -jar` is executed on the produced JAR
- **THEN** the application starts without needing a classpath or main-class argument
