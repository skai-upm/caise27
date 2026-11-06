# Spec Delta

## ADDED Requirements

### Requirement: Delivery 1 password rules applied before protection
The password protection configured at application start SHALL apply the password validation rules of the user data validation (uppercase, lowercase, digit and symbol) before producing a protected value.

#### Scenario: Weak password not protected
- **GIVEN** the application has been initialized
- **WHEN** protection of `clave1!` is requested
- **THEN** it is rejected with an invalid-input error and no protected value is produced
