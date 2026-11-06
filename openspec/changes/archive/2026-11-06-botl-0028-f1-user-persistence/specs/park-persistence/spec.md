# Spec Delta

## ADDED Requirements

### Requirement: Users survive restarts
Registered users SHALL be stored in a text file in the data directory, one user per line, with attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`. The password field SHALL contain the stored protected (BCrypt) value. Users SHALL be loaded before any other element when the application starts. Reading and writing SHALL be done by a dedicated repository class, not by the user class or the main class. Malformed or duplicate lines SHALL be reported as warnings and ignored.

#### Scenario: Users recovered after restart
- **GIVEN** a run in which `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` was processed
- **WHEN** the application starts again with the same data directory
- **THEN** user `ana@correo.com` exists with the same first name, surnames, DNI, phone and card, and their stored password still verifies `Clave1!`

#### Scenario: Line mapped to fields
- **GIVEN** the users file line `Pedro;Lopez Martin;87654321X;pedro@correo.com;<bcrypt>;623456789;9876543210987654`
- **WHEN** it is loaded
- **THEN** first name `Pedro`, surnames `Lopez Martin`, DNI `87654321X`, e-mail `pedro@correo.com`, phone `623456789` and card `9876543210987654` are assigned to the corresponding attributes

#### Scenario: Plain password never written
- **WHEN** the users file is written after a registration with password `Clave1!`
- **THEN** the file does not contain `Clave1!`

#### Scenario: Duplicate or malformed lines
- **GIVEN** a users file with a line of 3 fields and a second line repeating an existing DNI
- **WHEN** the application starts
- **THEN** a warning is shown for each line and only the valid users are loaded
