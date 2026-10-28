## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
The application SHALL target Java 21 and parse commands with basic `String` operations and `split`, without regular expressions, databases or frameworks. External runtime libraries and encryption mechanisms SHALL NOT be used, except the jBCrypt library (`org.mindrot:jbcrypt`) introduced in Delivery 3 for password protection, which SHALL be packaged inside the executable JAR.

#### Scenario: No external runtime dependencies
- **WHEN** the project's `pom.xml` is inspected
- **THEN** every declared dependency has `test` scope except `org.mindrot:jbcrypt`, and the compiler release is 21

#### Scenario: Self-contained executable JAR
- **WHEN** `java -jar target/amusement-park.jar` runs an operation that protects a password
- **THEN** the BCrypt classes are found inside the JAR without any extra classpath
