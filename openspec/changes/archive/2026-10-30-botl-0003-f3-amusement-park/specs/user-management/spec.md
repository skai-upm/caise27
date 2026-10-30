# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL locate the user by email, verify the password against the stored password hash, delete the user and update persistent storage. If the email does not exist or the password is wrong, nothing SHALL be deleted. Deletion SHALL also be rejected while the user has any ticket (individual or within a group ticket) dated on or after the system date. A deleted user SHALL be removed from every group; past tickets SHALL be kept as history.

#### Scenario: Authenticated deletion
- **GIVEN** a user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the persistent file no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` receives a nonexistent email or an incorrect password
- **THEN** no user is deleted and an error message is reported

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Historical ticket does not block deletion
- **GIVEN** the ticket above and `SYSTEM DATE;2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** Ana is deleted, removed from her groups, and `T001` remains as a historical record

#### Scenario: Group ticket member cannot be deleted
- **GIVEN** Ana belongs to a group ticket dated on or after the system date
- **WHEN** Ana's deletion is requested
- **THEN** deletion is rejected

### Requirement: Update user
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (represented by an enum). It SHALL verify the current email and the password against the stored password hash before modifying anything, revalidate the new value with the creation rules, and persist successful changes; a new password SHALL be stored as a new hash. `DNI` SHALL NOT be updatable.

#### Scenario: Update first name
- **GIVEN** a user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name becomes `Ana Maria` and is persisted

#### Scenario: Wrong credentials on update
- **WHEN** `USER UPDATE` receives a nonexistent email or an incorrect password
- **THEN** no change is made

#### Scenario: Email already used by another user
- **GIVEN** two registered users
- **WHEN** one tries to change their email to the other's email
- **THEN** the update is rejected

#### Scenario: Invalid new password or card
- **WHEN** an update supplies a password failing the character rules or a card failing the 16-digit rule
- **THEN** the update is rejected and the user's state is unchanged

#### Scenario: Unknown or non-editable field
- **WHEN** the field is `DNI` or any value outside the editable set
- **THEN** the update is rejected and the user's state is unchanged
