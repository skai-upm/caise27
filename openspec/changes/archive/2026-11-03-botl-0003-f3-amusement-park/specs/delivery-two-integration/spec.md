# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery-2 technical restrictions
The project SHALL use Java 21 and Maven, build with `mvn package` into an executable JAR, use no databases, persistence frameworks or external libraries other than the BCrypt library required by Delivery 3, and keep in-memory relationships as object references in the mandated directions.

#### Scenario: Fresh build and run
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** the JAR runs interactively, with a command file, and with `--fly`
