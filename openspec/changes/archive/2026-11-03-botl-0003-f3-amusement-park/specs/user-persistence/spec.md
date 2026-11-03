# Spec Delta

## MODIFIED Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file, one user per line, as `FirstName;Surnames;DNI;Email;Password;Phone;Card`, where the Password column holds the BCrypt hash of the password, never the original password.

#### Scenario: Stored line format
- **WHEN** Ana is created with the source example data
- **THEN** the file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash of `Clave1!` and `;612345678;1234567890123456`
