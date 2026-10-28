# Spec Delta

## ADDED Requirements

### Requirement: Executable JAR packaging
`mvn package` SHALL produce the JAR file `target/amusement-park.jar` whose manifest declares the application entry point, so that it can be started with `java -jar` independently of the development environment.

#### Scenario: Package produces runnable JAR
- **WHEN** `mvn package` is executed in the repository root
- **THEN** `target/amusement-park.jar` exists and `java -jar target/amusement-park.jar` starts the application without requiring a classpath or main class argument
