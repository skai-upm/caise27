# user-groups Specification

## Purpose
Defines groups of registered users: their creation with a unique identifier and name, membership management without duplicates, deletion, and the rule that active group tickets lock a group.

## Requirements

### Requirement: Group creation
The command `GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name. The identifier SHALL be unique among existing groups, and both fields SHALL be non-empty; otherwise the command SHALL be rejected with an error message and no group SHALL be created or changed.

#### Scenario: Create an empty group
- **GIVEN** no group `G01` exists
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a confirmation is shown and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **GIVEN** group `G01` exists named `Familia Garcia`
- **WHEN** `GROUP CREATE;G01;Otros` is processed
- **THEN** an error is shown and `G01` is still named `Familia Garcia`

#### Scenario: Missing fields rejected
- **WHEN** `GROUP CREATE;G02` or `GROUP CREATE;;Name` is processed
- **THEN** an error is shown and no group is created

### Requirement: Adding users to a group
The command `GROUP ADD;Identifier;Email` SHALL add the existing user with that e-mail to the group. It SHALL be rejected with an error, leaving the group unchanged, when the group does not exist, the user does not exist, the user already belongs to that group, or the group is locked.

#### Scenario: Add existing users progressively
- **GIVEN** an empty group `G01` and registered users `ana@correo.com` and `pedro@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both are members of `G01`

#### Scenario: Same user cannot be added twice
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error is shown and `ana@correo.com` appears only once in `G01`

#### Scenario: Unknown user or group rejected
- **WHEN** `GROUP ADD;G01;nobody@correo.com` (unregistered user) or `GROUP ADD;G99;ana@correo.com` (no such group) is processed
- **THEN** an error is shown and no membership changes

### Requirement: Removing users from a group
The command `GROUP REMOVE;Identifier;Email` SHALL remove the user from the group. It SHALL be rejected with an error, leaving the group unchanged, when the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Remove a member
- **GIVEN** `ana@correo.com` is a member of unlocked group `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer a member

#### Scenario: Remove a non-member rejected
- **GIVEN** `pedro@correo.com` is not a member of `G01`
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error is shown and members are unchanged

### Requirement: Group deletion
The command `GROUP DELETE;Identifier` SHALL delete an existing, unlocked group. Deleting a missing or locked group SHALL be rejected with an error and the group SHALL remain.

#### Scenario: Delete a group without active tickets
- **GIVEN** group `G01` with only group tickets dated before the current system date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** a confirmation is shown and `G01` no longer exists

#### Scenario: Delete a locked group rejected
- **GIVEN** group `G01` with a group ticket dated on the current system date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error is shown and `G01` still exists

### Requirement: Derived group lock
A group SHALL be locked exactly when at least one associated group ticket has a date equal to or later than the current system date. The locked state SHALL NOT be stored; it SHALL be evaluated from the group's tickets and the current system date at each operation, so removing the last active ticket or advancing the system date past all tickets makes the group modifiable again.

#### Scenario: Active ticket locks membership changes
- **GIVEN** group `G01` with a ticket dated after the current system date
- **WHEN** `GROUP ADD` or `GROUP REMOVE` is processed for `G01`
- **THEN** an error is shown and members are unchanged

#### Scenario: Historical tickets do not lock
- **GIVEN** group `G01` whose tickets are all dated before the current system date
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed for a registered non-member
- **THEN** the user is added

#### Scenario: Lock re-evaluated when the date changes
- **GIVEN** group `G01` with a single ticket dated 2027-04-15 and the system date 2027-04-15
- **WHEN** the system date is changed to 2027-04-16
- **THEN** `G01` can be modified again
