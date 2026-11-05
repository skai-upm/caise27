## ADDED Requirements

### Requirement: Self-contained executable JAR
The executable JAR produced by `mvn package` SHALL include its runtime dependencies (the BCrypt library), so `java -jar target/amusement-park.jar` runs without any additional classpath configuration.

#### Scenario: Password operations work from the packaged JAR
- **GIVEN** only `target/amusement-park.jar` copied to an empty directory
- **WHEN** it is run with `USER CREATE` and `USER DELETE` commands for the same user and password
- **THEN** `USER CREATED` and `USER DELETED` are printed without class-loading errors
