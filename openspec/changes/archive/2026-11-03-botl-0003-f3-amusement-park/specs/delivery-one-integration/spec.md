# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery-1 technical restrictions
The production code SHALL use Java 21 and the Java standard library, with no databases, frameworks, regular expressions or external libraries other than the BCrypt library that Delivery 3 explicitly requires for password protection, and SHALL build from scratch with `mvn package` into a JAR runnable outside an IDE.

#### Scenario: Fresh build
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** the executable JAR is produced and runs both interactively and with a command file

#### Scenario: Restriction inspection
- **WHEN** the production sources and `pom.xml` are inspected
- **THEN** the only runtime dependency is the BCrypt library and there is no use of regular expressions or other encryption
