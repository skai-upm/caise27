# Spec Delta

## MODIFIED Requirements

### Requirement: User file format
The system SHALL store users in a text file, one user per line, with the fields `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` separated by `;`, where `Contraseña` is the BCrypt hash of the user's password.

#### Scenario: Line written for a created user
- **WHEN** user Ana from the creation example is created
- **THEN** the file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`
