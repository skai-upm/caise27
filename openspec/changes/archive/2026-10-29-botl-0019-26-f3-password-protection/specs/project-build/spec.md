# Spec Delta

## MODIFIED Requirements

### Requirement: Java standard library only
The application's runtime code SHALL depend only on the Java standard library and, from Delivery 3, on the BCrypt library used to protect passwords: no databases, frameworks, other external libraries or regular expressions are used. The BCrypt library SHALL be packaged in the executable JAR.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** every declared dependency has `test` scope except the BCrypt library `org.mindrot:jbcrypt`

#### Scenario: JAR self-contained
- **WHEN** `java -jar target/amusement-park.jar` runs on a machine with only a Java runtime
- **THEN** the BCrypt classes are found inside the JAR
