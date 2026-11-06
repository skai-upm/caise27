# user-groups Specification

## Purpose
Lets registered users be organized into named groups with unique identifiers, as a basis for group tickets. Group membership can change only while the group has no active group ticket.

## Requirements

### Requirement: Create a group
The system SHALL create a group with `GROUP CREATE;Identifier;Name` when the identifier and name are non-empty and no group with that identifier exists. The new group has no members. Otherwise it SHALL reject the command with an error message and leave existing groups unchanged.

#### Scenario: Create an empty group
- **GIVEN** no group `G01` exists
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a confirmation is displayed
- **AND** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier is rejected
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an error message is displayed
- **AND** group `G01` keeps its original name and members

#### Scenario: Missing fields are rejected
- **WHEN** `GROUP CREATE;G02` or `GROUP CREATE;;Nombre` is processed
- **THEN** an error message is displayed and no group is created

### Requirement: Add an existing user to a group
The system SHALL add the user with the given email to the group with `GROUP ADD;Identifier;Email` when the group exists, the user exists in the system, the user is not already a member and the group has no active group ticket. Otherwise it SHALL reject the command with an error message and leave the membership unchanged.

#### Scenario: Add users progressively
- **GIVEN** an empty group `G01` and registered users `ana@correo.com` and `pedro@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both are confirmed and `G01` has both users as members

#### Scenario: Same user cannot be added twice
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message is displayed
- **AND** `ana@correo.com` appears only once in `G01`

#### Scenario: Unknown user or group is rejected
- **WHEN** `GROUP ADD` names a group that does not exist or an email that does not belong to a registered user
- **THEN** an error message is displayed and no membership changes

### Requirement: Remove a user from a group
The system SHALL remove the user with the given email from the group with `GROUP REMOVE;Identifier;Email` when the group exists, the user is a member and the group has no active group ticket. Otherwise it SHALL reject the command with an error message and leave the membership unchanged.

#### Scenario: Remove a member
- **GIVEN** `ana@correo.com` is a member of `G01`, which has no active group ticket
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** a confirmation is displayed and `ana@correo.com` is no longer a member

#### Scenario: Removing a non-member is rejected
- **GIVEN** `pedro@correo.com` is not a member of `G01`
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error message is displayed and the membership is unchanged

### Requirement: Group lock derived from active group tickets
The system SHALL consider a group locked when at least one associated group ticket has a date equal to or later than the current system date. While locked, it SHALL reject GROUP ADD and GROUP REMOVE. Group tickets dated before the current date are historical and SHALL NOT lock the group. The locked state SHALL be computed from the group's tickets and the current date, not stored.

#### Scenario: Active group ticket blocks membership changes
- **GIVEN** group `G01` has a group ticket dated on or after the current system date
- **WHEN** `GROUP ADD` or `GROUP REMOVE` is processed for `G01`
- **THEN** an error message states that the group cannot be modified
- **AND** the membership is unchanged

#### Scenario: Only historical tickets do not block
- **GIVEN** every group ticket of `G01` is dated before the current system date
- **WHEN** `GROUP ADD` is processed for a registered non-member
- **THEN** the user is added

#### Scenario: Lock follows the system date
- **GIVEN** group `G01` has a group ticket dated 2027-04-15
- **WHEN** the system date changes from 2027-04-15 to 2027-04-16
- **THEN** `G01` becomes modifiable without any other change

### Requirement: Delete a group
The system SHALL delete a group with `GROUP DELETE;Identifier` only if the group exists and has no associated group ticket dated on or after the current system date. Otherwise it SHALL reject the command with an error message and keep the group.

#### Scenario: Delete a group without tickets
- **GIVEN** group `G01` exists and has no group tickets
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** a confirmation is displayed and `G01` no longer exists

#### Scenario: Ticket today or later blocks deletion
- **GIVEN** group `G01` has a group ticket dated today or later
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error message is displayed and `G01` still exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and `G99` does not exist
- **THEN** an error message is displayed

### Requirement: GROUP commands listed in HELP
The system SHALL include the syntax of GROUP CREATE, GROUP ADD, GROUP REMOVE and GROUP DELETE in the output of `HELP`.

#### Scenario: HELP shows GROUP commands
- **WHEN** `HELP` is processed
- **THEN** the output contains `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>` and `GROUP DELETE;<Identifier>`
