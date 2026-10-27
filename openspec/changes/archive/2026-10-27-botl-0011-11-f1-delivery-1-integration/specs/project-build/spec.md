# Spec Delta

## ADDED Requirements

### Requirement: Delivery 1 technical restrictions
The application SHALL use only the Java standard library at runtime and SHALL NOT use databases, frameworks, external libraries, regular expressions or encryption mechanisms. Commands SHALL be processed with basic String operations and `split`, persistence SHALL be file based, and the user, main and persistence responsibilities SHALL be in separate classes with encapsulated attributes.

#### Scenario: No runtime dependencies
- **WHEN** the Maven dependencies with compile or runtime scope are listed
- **THEN** there are none (test-scope dependencies only)

#### Scenario: No forbidden mechanisms in sources
- **WHEN** the production sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`/`replaceAll`, JDBC, object serialization or encryption APIs

### Requirement: Build from scratch
A fresh clone of the repository SHALL build with `mvn package` and produce the executable JAR without IDE files or additional configuration.

#### Scenario: Fresh clone build
- **WHEN** the repository is cloned into an empty directory and `mvn package` is run there
- **THEN** the build succeeds and `java -jar target/amusement-park.jar input.txt` processes the command file
