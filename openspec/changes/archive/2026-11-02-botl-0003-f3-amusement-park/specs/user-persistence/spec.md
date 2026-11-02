## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file, one user per line, with attributes in the order first name, surnames, DNI, email, password, telephone, card, separated by `;` (for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`). From Delivery 3 the password attribute SHALL hold the BCrypt hash of the password instead of the password itself.

#### Scenario: Created user written as one line
- **WHEN** a user is successfully created
- **THEN** the users file contains one line for that user with its seven attributes separated by `;` in the defined order

#### Scenario: Password column holds a hash
- **WHEN** a user is created from Delivery 3 onward
- **THEN** the fifth attribute of its line is a BCrypt hash and the plaintext password does not appear in the file
