# Spec Delta

## ADDED Requirements

### Requirement: Passwords protected with BCrypt
User passwords SHALL NOT be kept in plain text. After passing the Delivery 1 password rules, a password SHALL be stored as a BCrypt value generated with the added library; when a password is changed, a new BCrypt value SHALL be generated.

#### Scenario: Password stored protected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the stored password of Ana is a BCrypt value different from `Clave1!` that BCrypt verifies against `Clave1!`

#### Scenario: Invalid password rejected before protection
- **WHEN** a user is created with password `clave1!`
- **THEN** the creation is rejected by the password rules and nothing is stored

#### Scenario: Password change generates a new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored value changes, BCrypt verifies it against `Nueva2#` and no longer against `Clave1!`

### Requirement: Password verification with BCrypt
Operations that require the user's password (USER DELETE, USER UPDATE) SHALL verify the entered password against the stored BCrypt value using the added library.

#### Scenario: Correct password accepted
- **WHEN** Ana's stored value protects `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the password is accepted and Ana is deleted

#### Scenario: Wrong password rejected
- **WHEN** `USER UPDATE;ana@correo.com;Otra1!;PHONE;699999999` is processed
- **THEN** an incorrect-password error is printed and nothing changes
