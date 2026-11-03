# Spec Delta

## MODIFIED Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number, where from Delivery 3 the password field holds the BCrypt hash of the password instead of the password itself (e.g. `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$...;612345678;1234567890123456`).

#### Scenario: Created user written as one line
- **WHEN** a user is created successfully
- **THEN** the user data file contains a line with that user's seven attributes separated by `;` in the specified order

#### Scenario: Password field holds a hash
- **WHEN** a user created with password `Clave1!` is stored
- **THEN** the fifth field of the line is a BCrypt hash and not `Clave1!`
