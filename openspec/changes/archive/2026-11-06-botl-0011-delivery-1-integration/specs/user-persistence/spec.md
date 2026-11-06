# Spec Delta

## MODIFIED Requirements

### Requirement: Users loaded at start-up
When the application starts it SHALL load every stored user from the user file, mapping each attribute to its field by position; if the file does not exist the application MUST start with no users and without error. If the user file exists but cannot be read, the application MUST print an error message and end normally without processing commands and without modifying the file.

#### Scenario: Users recovered after restart
- **GIVEN** users were created in a previous run
- **WHEN** the application is started again
- **THEN** those users exist with the same attributes, and creating another user with one of their DNIs or emails is rejected as a duplicate

#### Scenario: Persisted line mapped to fields
- **GIVEN** the user file line `Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`
- **WHEN** the users are loaded
- **THEN** the user with DNI `87654321X` has first name `Pedro`, surnames `Lopez Martin`, email `pedro@correo.com`, password `Otra2#`, telephone `623456789` and card `9876543210987654`

#### Scenario: Missing file
- **GIVEN** no user file exists
- **WHEN** the application starts
- **THEN** it starts normally with no users

#### Scenario: Malformed line ignored
- **GIVEN** a user file containing a valid line and a line without exactly seven `;`-separated fields
- **WHEN** the users are loaded
- **THEN** the valid user is loaded, the malformed line is skipped, and the application still starts

#### Scenario: Unreadable user file
- **GIVEN** the user file path exists but cannot be read (for example, it is a directory)
- **WHEN** the application starts with commands on its input
- **THEN** an error message about loading users is printed, no command is processed, no stack trace is shown, and the process ends normally
