## MODIFIED Requirements

### Requirement: User file format
The system SHALL store users in a text file with one user per line and fields in the order `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, where `Contraseña` is the BCrypt hash of the password.

#### Scenario: Stored line
- **WHEN** the user `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` has been created
- **THEN** the user file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`
