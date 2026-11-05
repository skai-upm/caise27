## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
The application SHALL use only the Java standard library at runtime (no databases, frameworks or external libraries; test-only dependencies excluded), except for the BCrypt library (`org.mindrot:jbcrypt`) mandated by Delivery 3 for password protection, SHALL persist data only in text files, SHALL NOT use regular expressions or encryption mechanisms in its production code other than BCrypt password hashing through that library, and SHALL interpret commands with basic String operations and `split`.

#### Scenario: No runtime dependencies
- **WHEN** the Maven dependencies are listed
- **THEN** every declared dependency has `test` scope, except `org.mindrot:jbcrypt`

#### Scenario: No regular expressions or encryption in production code
- **WHEN** the production sources are inspected
- **THEN** they use no `java.util.regex`, no `matches`/`replaceAll`/`replaceFirst`, no `split` with a regular-expression pattern, and no `javax.crypto` or `java.security.MessageDigest`

#### Scenario: Basic execution example works from a clean build
- **GIVEN** `mvn package` has been run on a clean checkout
- **WHEN** the assignment's basic example (create Ana and Pedro, update Ana's phone and email, delete Pedro, HELP, EXIT) is run interactively and as a command file
- **THEN** both runs print `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text and the exit acknowledgement, and leave only Ana (with the new phone and email) in `users.txt`
