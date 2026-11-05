## MODIFIED Requirements

### Requirement: Standard library only
The application SHALL depend only on the Java standard library at runtime, with the single exception of the jBCrypt library (`org.mindrot:jbcrypt:0.4`) used to protect user passwords: the Maven project MUST NOT declare any other dependency with compile or runtime scope (test-scoped dependencies are allowed), and the main sources MUST NOT use regular-expression APIs, databases, frameworks or encryption mechanisms other than jBCrypt for passwords.

#### Scenario: No runtime dependencies
- **WHEN** the effective dependencies of the Maven project are inspected
- **THEN** every declared dependency has `test` scope except `org.mindrot:jbcrypt`

#### Scenario: No regular-expression APIs in main sources
- **WHEN** the main sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`, `replaceAll` or `replaceFirst`
