# Spec Delta

## Purpose

Defines groups of registered users: how they are created, how members are added and removed, how they are deleted, and how the group's locked state is derived from its active group tickets and the current system date.

## ADDED Requirements

### Requirement: GROUP CREATE command
The command `GROUP CREATE;Identifier;Name` SHALL create an empty group with that unique identifier and name and print `GROUP CREATED`. An identifier already used by another group, or an empty identifier or name, SHALL be rejected with an `ERROR:` message.

#### Scenario: Create an empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** `GROUP CREATED` is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an `ERROR:` line about the existing group is printed and group `G01` keeps its name

### Requirement: GROUP ADD command
The command `GROUP ADD;Identifier;Email` SHALL add the existing user with that email to the group and print `USER ADDED TO GROUP`. It SHALL be rejected with an `ERROR:` message if the group does not exist, no user has that email, the user already belongs to that group, or the group is locked.

#### Scenario: Add users progressively
- **WHEN** group `G01` exists and `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed for existing users Ana and Pedro
- **THEN** both print `USER ADDED TO GROUP` and `G01` contains Ana and Pedro

#### Scenario: User already in the group
- **WHEN** Ana belongs to `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `ERROR:` line about the duplicate member is printed and Ana appears only once in `G01`

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed with no such user, or `GROUP ADD;G99;ana@correo.com` with no such group
- **THEN** an `ERROR:` line is printed and no group changes

### Requirement: GROUP REMOVE command
The command `GROUP REMOVE;Identifier;Email` SHALL remove that user from the group and print `USER REMOVED FROM GROUP`. It SHALL be rejected with an `ERROR:` message if the group does not exist, the user is not a member of the group, or the group is locked.

#### Scenario: Remove a member
- **WHEN** Ana belongs to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `USER REMOVED FROM GROUP` is printed and Ana is no longer a member of `G01`

#### Scenario: Not a member
- **WHEN** Ana does not belong to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an `ERROR:` line is printed

### Requirement: GROUP DELETE command
The command `GROUP DELETE;Identifier` SHALL delete the group and print `GROUP DELETED`, unless the group does not exist or it has an associated group ticket dated on or after the current system date, in which case an `ERROR:` message SHALL be printed and the group kept. Group tickets dated before the current system date SHALL NOT prevent deletion.

#### Scenario: Delete a group without active tickets
- **WHEN** group `G01` has no group ticket dated on or after the current system date and `GROUP DELETE;G01` is processed
- **THEN** `GROUP DELETED` is printed and `G01` no longer exists

#### Scenario: Delete a group with an active ticket
- **WHEN** group `G01` has a group ticket dated on or after the current system date and `GROUP DELETE;G01` is processed
- **THEN** an `ERROR:` line about the active group ticket is printed and `G01` still exists

### Requirement: Derived group lock
A group SHALL be locked exactly when at least one associated group ticket has a date equal to or later than the current system date. While locked, `GROUP ADD` and `GROUP REMOVE` SHALL be rejected. The locked state SHALL NOT be stored as an attribute; it SHALL be evaluated from the existing group tickets and the current system date each time, so that it changes when tickets are deleted or the system date moves past them.

#### Scenario: Locked group cannot change members
- **WHEN** group `G01` has a group ticket dated on or after the current system date and `GROUP ADD` or `GROUP REMOVE` is processed for `G01`
- **THEN** an `ERROR:` line about the locked group is printed and the members are unchanged

#### Scenario: Only historical tickets
- **WHEN** all group tickets of `G01` are dated before the current system date
- **THEN** `G01` is not locked and its members can be changed
