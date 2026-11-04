# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated user records
The system SHALL store each user on one line of a text file with fields in the order `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, where `Contraseña` is the BCrypt hash of the user's password.

#### Scenario: File content after creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`
