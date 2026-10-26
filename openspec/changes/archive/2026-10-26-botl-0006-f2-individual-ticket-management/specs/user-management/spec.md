# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The command `USER DELETE;Correo;Contraseña` SHALL delete the user with that email only when the user exists, the password matches the stored password, and the user has no ticket dated on or after the system date; it SHALL update the persistent file. When the deletion succeeds, the user's remaining (past) tickets SHALL also be removed. Otherwise no user is deleted and an `ERROR` line is printed.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is received and that user exists with password `Clave1!`
- **THEN** the user is removed from the system and the file, and `OK USER DELETED` is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is received with an email that is not registered
- **THEN** no user is deleted and an `ERROR` line is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is received with a registered email and an incorrect password
- **THEN** no user is deleted and an `ERROR` line is printed

#### Scenario: Future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana has ticket `T001` dated 2026-10-15, and `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the deletion is rejected with an `ERROR` line and Ana remains registered

#### Scenario: Ticket for today blocks deletion
- **WHEN** the system date is 2026-10-15 and Ana has a ticket dated 2026-10-15
- **THEN** `USER DELETE` for Ana is rejected with an `ERROR` line

#### Scenario: Past ticket does not block deletion
- **WHEN** the system date is 2026-10-16 and Ana's only ticket is dated 2026-10-15
- **THEN** `USER DELETE` for Ana succeeds and her past ticket is removed
