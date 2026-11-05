## MODIFIED Requirements

### Requirement: Deletion with correct credentials
The system SHALL locate the user by email, verify that it exists, verify the given password against the user's stored BCrypt-protected password with the jBCrypt library, delete the user, update the persisted users file, and print a response starting with `OK`.

#### Scenario: Matching email and password
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** a response starting with `OK` is printed
- **AND** the user no longer exists
- **AND** the users file no longer contains the user's line

#### Scenario: Deleted user's DNI and email become available
- **WHEN** a user has been deleted
- **THEN** a later `USER CREATE` with the same DNI and email succeeds

#### Scenario: Deletion survives restart
- **WHEN** a user is deleted and the application is started again
- **THEN** the deleted user is not loaded
