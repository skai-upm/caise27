# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The command `USER DELETE;Correo;Contraseña` SHALL delete the user with that email only if the user exists, the supplied password matches the stored password, and the user has no ticket (individual or part of a group ticket) dated on or after the system date. On deletion the user SHALL also be removed from every group it belonged to, and the stored data SHALL then be updated.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and the user exists with that password
- **THEN** the user is deleted, the stored data no longer contains it, and an `OK` result is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email that does not belong to any user
- **THEN** no user is deleted and an `ERROR` result is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** the user is not deleted and an `ERROR` result is printed

#### Scenario: Current or future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana has ticket T001 dated 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is not deleted and an `ERROR` result is printed

#### Scenario: Past ticket does not block deletion
- **WHEN** the system date is 2026-10-16 and Ana's only ticket is dated 2026-10-15
- **THEN** `USER DELETE;ana@correo.com;Clave1!` deletes the user and the ticket is kept as history

#### Scenario: Deleted user leaves its groups
- **WHEN** Ana is a member of an unlocked group G01 and is deleted
- **THEN** G01 no longer contains Ana
