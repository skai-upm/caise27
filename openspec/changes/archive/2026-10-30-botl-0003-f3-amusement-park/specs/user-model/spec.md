# Spec Delta

## MODIFIED Requirements

### Requirement: User attributes
Each user SHALL hold a first name, surname(s), DNI, email address, protected (BCrypt-hashed) password, telephone number and card number, all accessible only through the user's public operations (encapsulated, not public fields). The plain password SHALL NOT be held by the user.

#### Scenario: User retains its data
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** querying the user returns those values for the six non-password attributes and a password hash that verifies `Clave1!` and differs from it
