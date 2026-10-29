# Spec Delta

## MODIFIED Requirements

### Requirement: User data persisted
Every user attribute SHALL be stored and reconstructed by the users repository in a text file with one user per line and the attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`, the password only in protected form; previously stored users SHALL be loaded when the application starts, and stored users with fewer fields SHALL still be read.

#### Scenario: Restart keeps all user data
- **WHEN** a complete user is saved and the application restarts
- **THEN** all seven attributes are reconstructed and the original password still verifies

#### Scenario: Assignment line format
- **WHEN** user Ana (`Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, password `Clave1!`, `612345678`, `1234567890123456`) is saved
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<protected password>;612345678;1234567890123456`, where the password field is a BCrypt value and not `Clave1!`

#### Scenario: Users created by command reloaded at start
- **WHEN** an execution creates Ana and Pedro with USER CREATE and a new execution starts in normal mode
- **THEN** both users exist with their data
