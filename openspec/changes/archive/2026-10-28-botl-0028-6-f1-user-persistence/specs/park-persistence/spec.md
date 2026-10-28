## MODIFIED Requirements

### Requirement: User attributes persisted
The user file SHALL store one user per line with the attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`, where Password is the protected (BCrypt) value, and restore them on the next execution; lines written by earlier versions of the application (with fewer fields or the previous attribute order) SHALL still load.

#### Scenario: Full user after restart
- **WHEN** a user with all attributes is saved and the application restarts
- **THEN** the user is restored with the same first name, surnames, DNI, e-mail, protected password, telephone and card

#### Scenario: Earlier user line
- **WHEN** the user file contains `12345678Z;ana@correo.com`
- **THEN** the user loads with that DNI and e-mail and empty remaining attributes

#### Scenario: Assignment line format
- **WHEN** user `Ana Garcia Lopez` (DNI `12345678Z`, e-mail `ana@correo.com`, telephone `612345678`, card `1234567890123456`) is saved
- **THEN** its line is `Ana;Garcia Lopez;12345678Z;ana@correo.com;<bcrypt>;612345678;1234567890123456`, where `<bcrypt>` is the protected password and not `Clave1!`

#### Scenario: Users created by command persist
- **WHEN** `USER CREATE` registers two users and the application is started again
- **THEN** both users are loaded with all their attributes and their passwords verify
