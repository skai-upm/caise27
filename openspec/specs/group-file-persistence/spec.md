# group-file-persistence Specification

## Purpose

Defines how groups and their memberships are stored in a text file and restored, linked to the existing users, when the application starts again.

## Requirements

### Requirement: Group file format
Groups SHALL be stored in a UTF-8 text file separate from the users file, one group per line, as `Id;Nombre` followed by one `;`-separated field per member containing that member's DNI. No database or external library SHALL be used.

#### Scenario: File content
- **GIVEN** group `G01` named `Familia Garcia` with members Ana (`12345678Z`) and Pedro (`87654321X`), added in that order
- **WHEN** the groups are saved
- **THEN** the groups file contains the line `G01;Familia Garcia;12345678Z;87654321X`

### Requirement: Group file reflects every successful change
After each successful group command, and after a user deletion that removed memberships, the groups file SHALL contain exactly the current groups and memberships. A rejected command SHALL NOT change it.

#### Scenario: Rejected group command
- **WHEN** a `GROUP` command is rejected
- **THEN** the groups file content is unchanged

### Requirement: Group restart recovery
On startup the application SHALL load the groups after the users and link each stored DNI to the loaded user with that DNI, without creating new user objects. A missing groups file SHALL mean no groups. A stored DNI with no matching user SHALL be ignored.

#### Scenario: Restore groups after restart
- **GIVEN** groups `G01` (Ana, Pedro) and `G02` (Ana) were created in a previous run
- **WHEN** the application restarts with the same files
- **THEN** both groups exist with the same members, and Ana in `G01` and in `G02` is the same user entity as the one returned by user lookup

#### Scenario: Unknown member on load
- **GIVEN** a groups file line refers to a DNI that no stored user has
- **WHEN** the application starts
- **THEN** the group is loaded without that member
