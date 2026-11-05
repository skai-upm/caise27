# Spec Delta

## ADDED Requirements

### Requirement: Standard library only
The application SHALL depend only on the Java standard library at runtime: the Maven project MUST NOT declare dependencies with compile or runtime scope (test-scoped dependencies are allowed), and the main sources MUST NOT use regular-expression APIs, databases, frameworks or encryption mechanisms.

#### Scenario: No runtime dependencies
- **WHEN** the effective dependencies of the Maven project are inspected
- **THEN** every declared dependency has `test` scope

#### Scenario: No regular-expression APIs in main sources
- **WHEN** the main sources are inspected
- **THEN** they contain no use of `java.util.regex`, `String.matches`, `replaceAll` or `replaceFirst`
