# password-protection Specification

## Purpose
Ensures that user passwords are never kept in plain text: new or changed passwords are validated and protected with BCrypt before storage, and authenticated operations verify entered passwords against the stored protected value.

## Requirements

### Requirement: Passwords are protected with BCrypt before storage
The system SHALL apply the password validation rules to every new or changed password and, if it is valid, produce a BCrypt-protected value to be stored instead of the password. The protected value SHALL NOT contain the original password, and protecting the same password twice SHALL produce different values (salted).

#### Scenario: Valid password is protected
- **WHEN** the password `Clave1!` is protected
- **THEN** the result is a BCrypt value (starting with `$2a$`) that does not contain `Clave1!`

#### Scenario: Same password, different stored values
- **WHEN** `Clave1!` is protected twice
- **THEN** the two protected values differ and both verify against `Clave1!`

#### Scenario: Invalid password is not protected
- **GIVEN** a password that does not satisfy the validation rules, such as a blank one
- **WHEN** protection is requested
- **THEN** it is rejected with an invalid-input error and no protected value is produced

### Requirement: Entered passwords are verified against the stored value
Operations that require authentication SHALL verify the entered password against the stored BCrypt value with the BCrypt library, accepting it only when it matches.

#### Scenario: Correct password
- **GIVEN** the stored value of `Clave1!`
- **WHEN** `Clave1!` is verified
- **THEN** verification succeeds

#### Scenario: Wrong password or corrupt stored value
- **WHEN** `Clave2!` is verified against the stored value of `Clave1!`, or any password is verified against a value that is not a BCrypt hash
- **THEN** verification fails without terminating the application

### Requirement: Password protection is configurable
The hashing algorithm and the validation rules SHALL be dependencies supplied to the password protection component at application initialization, so the rules of the user milestones can be plugged in without changing it.

#### Scenario: Stricter policy supplied
- **GIVEN** the password component is configured with a policy that requires at least 6 characters
- **WHEN** `abc` is protected
- **THEN** it is rejected
