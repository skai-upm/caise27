# Spec Delta

## ADDED Requirements

### Requirement: Password protection with BCrypt
A new or changed password SHALL first pass the password validation rules, and then only its BCrypt hash SHALL be stored. Password-checking operations (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt. A password change SHALL store a newly generated hash. Files from earlier deliveries need not be migrated.

#### Scenario: Correct password verified against hash
- **WHEN** a user created with `Clave1!` runs `USER UPDATE;<email>;Clave1!;PHONE;699999999`
- **THEN** the update succeeds

#### Scenario: Wrong password rejected
- **WHEN** `USER DELETE;<email>;Wrong1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Password change rehashed
- **WHEN** `USER UPDATE;<email>;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored hash changes, `Nueva2#` authenticates, `Clave1!` no longer does, and no plain password is stored

#### Scenario: Invalid new password not stored
- **WHEN** `USER UPDATE;<email>;Clave1!;PASSWORD;clave1!` is processed
- **THEN** an error is printed and the stored hash is unchanged
