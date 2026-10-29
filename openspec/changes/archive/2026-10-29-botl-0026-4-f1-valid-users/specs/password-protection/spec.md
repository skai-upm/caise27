# Spec Delta

## ADDED Requirements

### Requirement: Password rules applied before storing
Before a new or changed password is protected and stored, the Delivery 1 password rules SHALL be applied; a password that does not satisfy them SHALL be rejected and the stored value SHALL remain unchanged.

#### Scenario: Weak password rejected
- **WHEN** the password of a user with password `Clave1!` is changed to `clave1!`
- **THEN** the change is rejected and `Clave1!` still verifies
