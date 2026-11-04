# group-file-persistence Specification

## Purpose
Defines how groups and their memberships are stored in a plain text file and restored as references to the existing users when the application starts.

## Requirements

### Requirement: Group file format
The system SHALL store groups in a text file, one group per line, with fields separated by `;`: identifier, name, then the DNI of each member in membership order.

#### Scenario: File contents
- **GIVEN** group `G01` named `Familia Garcia` with members DNI `12345678Z` and `87654321X`
- **WHEN** the groups are saved
- **THEN** the file contains `G01;Familia Garcia;12345678Z;87654321X`

### Requirement: Persist after successful group changes
After each successful group command, and after a user deletion that changes memberships, the groups file SHALL reflect the complete current set of groups; failed commands SHALL NOT change it.

#### Scenario: Rejected command leaves file untouched
- **GIVEN** a saved group `G01`
- **WHEN** a `GROUP ADD` for a nonexistent user is rejected
- **THEN** the groups file content is unchanged

### Requirement: Restore groups at startup
At startup the system SHALL load the groups file after the users, linking each member to the already loaded user with that DNI so no independent user copies are created. A missing file SHALL mean no groups; member DNIs without a matching user SHALL be ignored.

#### Scenario: Restart restores memberships
- **GIVEN** a previous run created `G01` and added Ana and Pedro
- **WHEN** the application starts again
- **THEN** `G01` contains exactly Ana and Pedro, and each member is the same object as the corresponding loaded user

#### Scenario: Email change survives restart
- **GIVEN** Ana is in `G01` and later changed her email to `ana.nueva@correo.com`
- **WHEN** the application restarts
- **THEN** `G01` still contains Ana with the new email
