# Spec Delta

## MODIFIED Requirements

### Requirement: Runtime uses only the Java standard library
The application SHALL depend at runtime only on the Java standard library plus the BCrypt library (`org.mindrot:jbcrypt`) used to protect passwords. That library SHALL be packaged inside the executable JAR, so `java -jar` needs no extra classpath. It SHALL NOT use databases, frameworks, regular expressions, or any other external library.

#### Scenario: No runtime dependencies packaged
- **WHEN** the built JAR is inspected
- **THEN** it contains only the application's own classes and resources plus the classes of the BCrypt library

#### Scenario: JAR runs standalone with BCrypt
- **WHEN** `java -jar target/amusement-park.jar` creates a user
- **THEN** the password is hashed without any additional classpath configuration
