# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Email;Password`; it SHALL locate the user by email, verify that the user exists, check that the password matches exactly, verify that deletion is allowed by the user's tickets and groups, remove the user from every group they belong to, delete the user and update the persisted user file, then print a confirmation naming the email. A command without exactly two fields MUST be rejected with an error message and change nothing.

#### Scenario: Matching email and password deletes the user
- **GIVEN** a stored user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** a confirmation containing `ana@correo.com` is printed, the user no longer exists, and the user file no longer contains its line

#### Scenario: Deleted user frees its DNI and email
- **GIVEN** the user `12345678Z` / `ana@correo.com` was deleted
- **WHEN** a valid `USER CREATE` with DNI `12345678Z` and email `ana@correo.com` is processed
- **THEN** the user is created

#### Scenario: Wrong number of fields
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error reporting the wrong number of fields is printed and no user is deleted

#### Scenario: Deleted user leaves their groups
- **GIVEN** `ana@correo.com` belongs to modifiable groups `G01` and `G02`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and is no longer a member of `G01` or `G02`

## ADDED Requirements

### Requirement: Deletion blocked by active tickets and locked groups
`USER DELETE` MUST be rejected with an error, changing nothing, while any ticket with a date equal to or later than the current system date is associated with the user, or while the user belongs to a group locked by an active group ticket. Historical tickets alone MUST NOT prevent deletion, and they MUST be retained with the information of the deleted user. This state is derived from the existing tickets and the current date, never stored on the user.

#### Scenario: Active ticket blocks deletion
- **GIVEN** the system date is `2027-04-15` and Ana has ticket `T001` dated `2027-04-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error reporting an active ticket is printed and Ana still exists

#### Scenario: Future ticket deleted first
- **GIVEN** Ana's only ticket `T001` is dated after the current date
- **WHEN** `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** both succeed

#### Scenario: Historical ticket does not block
- **GIVEN** Ana's only ticket `T001` is dated before the current system date
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and ticket `T001` still exists with Ana's data

#### Scenario: Locked group blocks deletion
- **GIVEN** Ana belongs to a group that has an active group ticket
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error reporting the locked group is printed and Ana and the group are unchanged
