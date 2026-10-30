# Spec Delta

## MODIFIED Requirements

### Requirement: Text file storage format
Users SHALL be stored in the text file `users.txt` in the data directory (the working directory, or the temporary directory in `--fly` mode), one user per line, with attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card.

#### Scenario: Stored line format
- **WHEN** user Ana Garcia Lopez (DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678`, card `1234567890123456`) is created
- **THEN** `users.txt` contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
