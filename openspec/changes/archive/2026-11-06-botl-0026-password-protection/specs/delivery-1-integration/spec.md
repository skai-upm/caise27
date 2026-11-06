# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
The production code SHALL use only the Java standard library plus, from Delivery 3 on, the BCrypt library mandated for password protection (`org.mindrot:jbcrypt`), with no databases, frameworks, other external libraries or regular expressions, and commands MUST be interpreted with basic String operations and `split`.

#### Scenario: No external runtime dependencies
- **WHEN** the Maven project's dependencies are inspected
- **THEN** the only non-test dependency is `org.mindrot:jbcrypt`

#### Scenario: No regular expressions
- **WHEN** the production sources are inspected
- **THEN** no regular-expression API (`Pattern`, `matches`, `replaceAll`, regex `split` patterns) is used
