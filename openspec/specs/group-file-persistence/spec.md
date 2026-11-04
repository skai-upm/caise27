# group-file-persistence Specification

## Purpose
Defines how groups and their member relationships are stored in a text file and restored, as references to the registered users, when the application starts again.

## Requirements

### Requirement: Group file format
The system SHALL store groups in a text file (`groups.txt`, next to the user file) with one group per line in the form `Identificador;Nombre;DNI1,DNI2,...`, identifying members by their DNI; a group without members SHALL have an empty third field.

#### Scenario: Stored line
- **GIVEN** group `G01` named `Familia Garcia` with members Ana (`12345678Z`) and Pedro (`87654321X`), added in that order
- **WHEN** the group file is inspected
- **THEN** it contains the line `G01;Familia Garcia;12345678Z,87654321X`

### Requirement: Group file updated after every successful change
After every successful group command, and after a user deletion that removed memberships, the group file SHALL reflect exactly the current groups and members; a rejected command SHALL leave it unchanged.

#### Scenario: Rejected command leaves file intact
- **WHEN** a `GROUP ADD` for a nonexistent user is rejected
- **THEN** the group file content is the same as before

### Requirement: Groups restored at startup
On startup the system SHALL load the groups after the users and resolve each stored DNI to the corresponding loaded user object, so restored members are the registered users and not independent copies; a missing group file SHALL mean no groups, and a DNI with no registered user SHALL be ignored.

#### Scenario: Restart restores relationships
- **GIVEN** groups `G01` (Ana, Pedro) and `G02` (Ana) were saved and the application stopped
- **WHEN** the application starts again
- **THEN** both groups exist with the same members, and Ana's member entry in `G01` and in `G02` is the same object as the registered user Ana

#### Scenario: Email change survives restart
- **GIVEN** Ana is in `G01` and her email was changed to `ana.nueva@correo.com`
- **WHEN** the application restarts and `GROUP REMOVE;G01;ana.nueva@correo.com` is processed
- **THEN** the removal succeeds

#### Scenario: First start without a group file
- **WHEN** the application starts and no group file exists
- **THEN** it starts with no groups and does not fail
