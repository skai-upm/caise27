# Spec Delta

## ADDED Requirements

### Requirement: Password hasher in the diagram
The class diagram SHALL show the `PasswordHasher` abstraction, its BCrypt implementation and the user controller's association with the abstraction.

#### Scenario: Hasher shown
- **WHEN** the diagram is reviewed
- **THEN** `BCryptPasswordHasher` realises `PasswordHasher` and `UserController` is associated with `PasswordHasher`
