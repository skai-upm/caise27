# user-management Specification

## Purpose
Defines the park user: its attributes, how they are encapsulated, and which of them may change, as the basis for user validation, registration, deletion and modification.

## Requirements

### Requirement: User attributes
Each user SHALL have a first name, surname(s), DNI, e-mail address, password, telephone number and card number, held as private attributes accessible only through the user's operations. The password SHALL be kept only in its protected (BCrypt) form.

#### Scenario: Complete user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, e-mail `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`
- **THEN** the user exposes those values (the password only as a protected value that verifies `Clave1!`)

#### Scenario: Encapsulation
- **WHEN** the user class is inspected
- **THEN** all its instance attributes are private

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after creation, while the other attributes MAY be modified through the user's operations.

#### Scenario: No way to change the DNI
- **WHEN** the operations of the user class are inspected
- **THEN** the DNI attribute is final and there is no operation that modifies it, whereas e-mail, names, telephone, card and protected password have modifiers
