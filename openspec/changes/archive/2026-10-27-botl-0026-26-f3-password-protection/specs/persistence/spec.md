# Spec Delta

## ADDED Requirements

### Requirement: Protected passwords in the users file
The users file SHALL contain the BCrypt-protected value of each password and SHALL NOT contain the original password; previously stored plain-text files are not migrated.

#### Scenario: File without plain passwords
- **WHEN** Ana is created with password `Clave1!` and the users file is inspected
- **THEN** the line for Ana contains a BCrypt value in the password position and the text `Clave1!` does not appear in the file

#### Scenario: Verification after restart
- **WHEN** the application restarts and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the stored BCrypt value is loaded and verifies the password, and the update succeeds
