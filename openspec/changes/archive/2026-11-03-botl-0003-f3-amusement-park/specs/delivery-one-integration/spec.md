# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, Maven with the standard structure, the Java standard library only at runtime, file-based persistence, and command processing with basic String operations and `split`; it SHALL NOT use databases, frameworks, external runtime libraries, regular expressions or encryption mechanisms. From Delivery 3 the BCrypt library added through Maven for password protection is the only permitted exception to the library and encryption restrictions.

#### Scenario: Runtime dependencies are standard library only
- **WHEN** the `pom.xml` and source code are inspected
- **THEN** apart from the Delivery 3 BCrypt dependency there are no non-test dependencies, and no regular expression APIs are used in the application code
