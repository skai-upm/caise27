# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, a standard-structure Maven project built with `mvn package`, an executable JAR, file-based persistence, command processing with basic `String` operations and `split`, separated user, main and persistence classes, and encapsulated attributes. It SHALL NOT use databases, frameworks, regular expressions, or external runtime libraries and encryption other than those a later delivery explicitly authorizes (the Delivery 3 BCrypt dependency for password protection).

#### Scenario: Restriction inspection
- **WHEN** `pom.xml` and the main sources are inspected
- **THEN** there are no runtime dependencies beyond the JDK and the authorized BCrypt library, and no regular expression APIs are used
