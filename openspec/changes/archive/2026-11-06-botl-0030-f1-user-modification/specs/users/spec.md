# Spec Delta

## ADDED Requirements

### Requirement: User modification
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL first check that the user with that e-mail exists and that the password matches the stored protected value. It SHALL then change one field from the enumerated set `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`, show `USER UPDATED` and persist the change. The new value SHALL satisfy the creation rules: a new e-mail SHALL be valid and unused by another user, a new password SHALL meet the password rules and be stored as a new protected value, a new card SHALL be exactly 16 digits, and name, surname and phone values SHALL be non-empty. Any failure SHALL reject the update with an error and leave the user unchanged.

#### Scenario: Valid update persisted
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed and the application restarts
- **THEN** the user's phone is `699999999`

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored value verifies `Nueva2#` and no longer verifies `Clave1!`

#### Scenario: Invalid new values rejected
- **WHEN** an update sets EMAIL `ana@correo.es`, EMAIL of another existing user, PASSWORD `nueva2#` or CARD `123`
- **THEN** an error is shown and the user is unchanged

#### Scenario: Wrong credentials
- **WHEN** the password does not match or the e-mail is unknown
- **THEN** an error is shown and nothing changes

### Requirement: DNI cannot be modified
Updates SHALL only target the enumerated modifiable fields. A request to modify `DNI`, or any field not in the set, SHALL be rejected with an error and the user SHALL remain unchanged.

#### Scenario: DNI change attempt
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is shown and the DNI remains `12345678Z`

### Requirement: E-mail change keeps relationships
When a user's e-mail changes, every park relationship that identifies the user by e-mail SHALL follow the change: group memberships, individual tickets (including group-ticket components and their passes) and attraction occupancy.

#### Scenario: Tickets and groups follow the new e-mail
- **GIVEN** `ana@correo.com` belongs to group `G01` and owns ticket `T001`
- **WHEN** her e-mail is changed to `ana.nueva@correo.com`
- **THEN** `G01` contains `ana.nueva@correo.com`, `T001` belongs to `ana.nueva@correo.com`, and her invoice still lists `T001`
