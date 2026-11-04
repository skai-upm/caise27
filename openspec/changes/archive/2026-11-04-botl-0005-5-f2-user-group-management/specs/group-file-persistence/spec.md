# Spec Delta

## Purpose

Defines how groups and their memberships are stored in a plain text file and restored on startup, linked to the users loaded by user persistence.

## ADDED Requirements

### Requirement: Group records
The system SHALL store each group on one line of a text file as `Identificador;Nombre` followed by one `;DNI` field per member in membership order; members are referenced by their immutable DNI.

#### Scenario: File content
- **GIVEN** group `G01` `Familia Garcia` with members Ana (`12345678Z`) and Pedro (`87654321X`)
- **WHEN** the groups are saved
- **THEN** the file contains the line `G01;Familia Garcia;12345678Z;87654321X`

### Requirement: Groups file reflects every successful change
After every successful group create, add, remove or delete, and after a user deletion that removed memberships, the groups file SHALL contain exactly the current groups and members; rejected commands SHALL leave it unchanged.

#### Scenario: Rejected group command
- **GIVEN** a persisted groups file
- **WHEN** a duplicate `GROUP ADD` is processed
- **THEN** the groups file is unchanged

### Requirement: Restart restoration
On startup the system SHALL load the groups file after the users, linking each member DNI to the loaded user record; a missing groups file SHALL mean no groups, and a member DNI with no loaded user or a malformed line SHALL be skipped with a warning.

#### Scenario: Restart restores relationships
- **GIVEN** groups `G01` (Ana, Pedro) and `G02` (Ana) were saved
- **WHEN** the application restarts with the same files
- **THEN** both groups are restored with the same members, and Ana in `G01` and Ana in `G02` are the same record as the registered user Ana

#### Scenario: No groups file
- **WHEN** the application starts without a groups file
- **THEN** it starts with no groups and without error
