# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
The application SHALL use only the Java standard library at runtime, except the BCrypt library (`org.mindrot:jbcrypt`) that Delivery 3 requires for password protection, and SHALL NOT use databases, frameworks, other external libraries, regular expressions or other encryption mechanisms. Commands SHALL be processed with basic String operations and `split`, persistence SHALL be file based, and the user, main and persistence responsibilities SHALL be in separate classes with encapsulated attributes.

#### Scenario: No runtime dependencies
- **WHEN** the Maven dependencies with compile or runtime scope are listed
- **THEN** the only one is `org.mindrot:jbcrypt` (all others are test-scope)

#### Scenario: No forbidden mechanisms in sources
- **WHEN** the production sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`/`replaceAll`, JDBC, object serialization or encryption APIs other than BCrypt

## ADDED Requirements

### Requirement: Runtime library bundled in the executable JAR
The executable JAR produced by `mvn package` SHALL contain the BCrypt library so that `java -jar target/amusement-park.jar` runs without additional classpath configuration.

#### Scenario: JAR runs standalone with BCrypt
- **WHEN** `mvn package` is run and the JAR is executed with `java -jar` in an empty directory, creating a user
- **THEN** the user is created and the stored password is a BCrypt value
