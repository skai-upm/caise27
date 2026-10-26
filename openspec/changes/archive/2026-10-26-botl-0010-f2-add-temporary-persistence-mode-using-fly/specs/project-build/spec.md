# Spec Delta

## ADDED Requirements

### Requirement: JAR file name
Packaging SHALL produce the executable JAR at `target/parque.jar`.

#### Scenario: Documented command works
- **WHEN** `mvn package` is run
- **THEN** `java -jar target/parque.jar` starts the application
