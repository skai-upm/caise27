# Spec Delta

## Purpose

Keeps groups and their memberships across application executions using a plain text file, without duplicating user data.

## ADDED Requirements

### Requirement: Group file format
The system SHALL store groups in a text file, one group per line, as `Id;Nombre;Miembros`, where `Miembros` is the comma-separated list of the members' DNIs (empty when the group has no members). User data SHALL NOT be copied into the group file.

#### Scenario: Line written for a group
- **GIVEN** group `G01` named `Familia Garcia` with members Ana (`12345678Z`) and Pedro (`87654321X`)
- **WHEN** the groups are saved
- **THEN** the file contains `G01;Familia Garcia;12345678Z,87654321X`

### Requirement: Save after successful group changes
After every successful GROUP CREATE, ADD, REMOVE or DELETE, and after a user deletion that changed a group, the group file SHALL reflect the current groups; rejected commands SHALL leave it unchanged.

#### Scenario: Rejected command leaves file intact
- **GIVEN** a stored group `G01`
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is rejected
- **THEN** the group file content is unchanged

### Requirement: Restore groups at startup
On startup the system SHALL load the groups after the users and link each stored DNI to the corresponding loaded user object. A missing group file SHALL mean no groups; a stored DNI with no registered user SHALL be ignored.

#### Scenario: Restart restores relationships
- **GIVEN** a previous run created `G01` with Ana and Pedro and removed Pedro
- **WHEN** the application restarts with the same files
- **THEN** `G01` contains exactly Ana, and it is the same user object returned by the user registry

#### Scenario: No group file yet
- **WHEN** the application starts without a group file
- **THEN** it starts with no groups and without error
