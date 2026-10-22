# Spec Delta

## MODIFIED Requirements

### Requirement: User file format
Users SHALL be stored in a text file named `users.txt` in the active data directory: the working directory in normal execution, or the run's temporary directory in `--fly` mode. The file holds one user per line, with attributes in the order first name, surnames, DNI, email, password, phone, card, separated by `;`.

#### Scenario: Stored line format
- **WHEN** the user Ana (`Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is stored
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

#### Scenario: Users file in temporary mode
- **WHEN** a user is created during a `--fly` execution
- **THEN** the user is written to `users.txt` inside the run's temporary directory, and the working directory's `users.txt` is not modified
