# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL process `USER DELETE;Correo;Contraseña` by finding the user with that email, checking that the password matches the stored password, checking that the user has no ticket dated on or after the current system date, removing the user and updating the persistent file. If the email is unknown, the password does not match, or such a ticket exists, nothing SHALL be deleted. Tickets dated before the system date SHALL NOT, by themselves, prevent the deletion. The individual tickets generated for the user by a group ticket SHALL count as the user's tickets.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the result starts with `OK` and the user no longer exists, also after a restart

#### Scenario: Wrong password or unknown email
- **WHEN** a deletion supplies a wrong password or an email that no user has
- **THEN** the result starts with `ERROR` and all users remain stored

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15` were processed
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed with the system date 2026-10-01 or 2026-10-15
- **THEN** the result starts with `ERROR` and the user still exists

#### Scenario: Past ticket does not block deletion
- **GIVEN** Ana's only ticket `T001` is dated 2026-10-15
- **WHEN** `SYSTEM DATE;2026-10-16` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the result starts with `OK` and the user is deleted while `T001` is kept

#### Scenario: Group ticket member
- **GIVEN** Ana belongs to `G01` and group ticket `TG01` for `G01` is dated 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed with the system date 2026-10-15, and again with 2026-10-16
- **THEN** the first deletion is rejected with `ERROR` and the second succeeds
