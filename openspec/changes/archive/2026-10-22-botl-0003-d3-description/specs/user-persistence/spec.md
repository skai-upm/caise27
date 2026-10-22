# Spec Delta

## MODIFIED Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file `users.txt` in the data directory (the working directory, or the temporary directory in `--fly` mode) with one user per line and attributes in the order first name, surnames, DNI, email, BCrypt password hash, phone, card, separated by `;`.

#### Scenario: Stored line
- **WHEN** Ana Garcia Lopez is created with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678`, card `1234567890123456`
- **THEN** the data file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

#### Scenario: Temporary mode location
- **WHEN** the application runs with `--fly` and a user is created
- **THEN** the line is written to `users.txt` inside the temporary directory and the working directory's `users.txt` is untouched
