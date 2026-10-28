# Spec Delta

## Purpose

Lets users that exist in the system be organised into identified, named groups whose membership can be changed while the group is modifiable.

## ADDED Requirements

### Requirement: GROUP CREATE command
The system SHALL accept `GROUP CREATE;Identifier;Name` and create an empty group with that identifier and name. The identifier SHALL be unique; a duplicate identifier, an empty identifier or name, or a wrong number of fields SHALL be rejected with an error message without creating a group.

#### Scenario: Create an empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** a group `G01` named `Familia Garcia` with no members exists

#### Scenario: Duplicate identifier rejected
- **WHEN** `GROUP CREATE;G01;Otro` is processed and group `G01` already exists
- **THEN** an error message is displayed and group `G01` keeps its original name

### Requirement: GROUP ADD command
The system SHALL accept `GROUP ADD;Identifier;Email` and add the existing user with that e-mail to the identified group when the group is modifiable. It SHALL reject with an error message an unknown group, an e-mail that does not belong to an existing user, a user already in that group, or a group that is not modifiable.

#### Scenario: Add existing users progressively
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist, group `G01` exists, and `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** group `G01` contains both users

#### Scenario: Duplicate membership rejected
- **WHEN** `ana@correo.com` already belongs to `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message is displayed and `ana@correo.com` appears only once in `G01`

#### Scenario: Unknown user or group rejected
- **WHEN** `GROUP ADD;G01;nadie@correo.com` (no such user) or `GROUP ADD;G99;ana@correo.com` (no such group) is processed
- **THEN** an error message is displayed and no membership changes

### Requirement: GROUP REMOVE command
The system SHALL accept `GROUP REMOVE;Identifier;Email` and remove that user from the identified group when the group is modifiable. It SHALL reject with an error message an unknown group, a user that is not a member of the group, or a group that is not modifiable.

#### Scenario: Remove a member
- **WHEN** `ana@correo.com` belongs to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer a member of `G01`

#### Scenario: Remove a non-member rejected
- **WHEN** `pedro@correo.com` does not belong to `G01` and `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error message is displayed and the membership of `G01` is unchanged

### Requirement: Derived group modifiability
A group SHALL be modifiable exactly when it has no associated group ticket whose date is equal to or later than the current system date. The modifiable (locked) state SHALL NOT be stored as an independent attribute; it SHALL be determined from the group's tickets and the current system date.

#### Scenario: Group without tickets is modifiable
- **WHEN** a group has no associated group tickets
- **THEN** GROUP ADD and GROUP REMOVE are permitted on it
