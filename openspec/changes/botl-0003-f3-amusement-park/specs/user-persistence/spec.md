# Spec Delta

## MODIFIED Requirements

### Requirement: Text-file user storage format
The system SHALL store users in a text file with one user per line and attributes separated by `;` in the order: first name, surname(s), DNI, email, protected password, telephone, card number. The protected password SHALL be the BCrypt hash of the password (or, for users written by an earlier delivery and not yet changed, the value originally stored). No database or external persistence library SHALL be used.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456` is stored
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`
