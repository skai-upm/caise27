## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, a standard-structure Maven project built with `mvn package`, an executable JAR, file-based persistence, command interpretation with basic String operations and `split`, and the Java standard library only at runtime. It SHALL NOT use databases, frameworks, external runtime libraries, regular expressions or encryption mechanisms. From Delivery 3 the BCrypt library added as a Maven dependency is permitted at runtime solely to protect stored passwords, superseding the no-external-library and no-encryption restrictions for that purpose only.

#### Scenario: Dependency and restriction inspection
- **WHEN** `pom.xml` and the production sources are inspected
- **THEN** there are no runtime dependencies beyond the JDK, and no use of regular expressions, databases or encryption

#### Scenario: BCrypt is the only runtime dependency from Delivery 3
- **WHEN** `pom.xml` is inspected after Delivery 3
- **THEN** the only non-test dependency is the BCrypt library
