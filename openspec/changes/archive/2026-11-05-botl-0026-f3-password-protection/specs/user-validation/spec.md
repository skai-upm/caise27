## MODIFIED Requirements

### Requirement: Whole-user validation
The system SHALL provide a single reusable check, applied to stored users, that reports a user's data as valid only when its DNI, email and card number are all valid and its password is a BCrypt-protected value (prefix `$2a$`, `$2b$` or `$2y$`, a two-digit cost, `$`, and 53 characters of the BCrypt alphabet `./A-Za-z0-9`), without modifying the user. The format check MUST NOT use regular expressions.

#### Scenario: Fully valid user
- **WHEN** a user with DNI `12345678Z`, email `ana@correo.com`, a BCrypt-protected password and card `1234567890123456` is checked
- **THEN** the data is reported valid

#### Scenario: Plain-text password makes the stored user invalid
- **WHEN** the same user data is checked but with password `Clave1!`
- **THEN** the data is reported invalid

#### Scenario: One invalid field makes the user invalid
- **WHEN** the same user data is checked but with card `123456789012345`
- **THEN** the data is reported invalid
- **AND** the user's attributes are unchanged
