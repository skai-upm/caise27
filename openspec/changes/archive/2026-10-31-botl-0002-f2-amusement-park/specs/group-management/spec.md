# Spec Delta

## Purpose

Lets registered users be organised into named groups that reference the existing users, so that groups can later buy tickets and access attractions together.

## ADDED Requirements

### Requirement: Group model
A group SHALL have a unique identifier, a name and a collection of members. Members SHALL be the registered users themselves, never independent copies of them.

#### Scenario: Member is the registered user
- **WHEN** Ana is added to group `G01` and then Ana's phone is changed with `USER UPDATE`
- **THEN** the member of `G01` reflects Ana's new phone

### Requirement: GROUP CREATE command
`GROUP CREATE;<id>;<name>` SHALL create an empty group. Creation SHALL be rejected when the identifier is already used by another group or when the identifier or name is blank.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed and no group `G01` exists
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate group identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otra` is executed
- **THEN** an error is reported and the existing group is unchanged

### Requirement: GROUP ADD command
`GROUP ADD;<groupId>;<email>` SHALL add the registered user with that email to the group. It SHALL be rejected if the group does not exist, the user does not exist, the user is already a member, or the group is locked.

#### Scenario: Add existing user
- **WHEN** group `G01` and user `ana@correo.com` exist and `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** Ana is a member of `G01`

#### Scenario: Unknown or duplicate member
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is executed for an unregistered email, or `GROUP ADD;G01;ana@correo.com` is executed while Ana is already a member
- **THEN** an error is reported and the membership of `G01` is unchanged

### Requirement: GROUP REMOVE command
`GROUP REMOVE;<groupId>;<email>` SHALL remove that user from the group. It SHALL be rejected if the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Remove member
- **WHEN** Ana is a member of unlocked group `G01` and `GROUP REMOVE;G01;ana@correo.com` is executed
- **THEN** Ana is no longer a member and remains a registered user

#### Scenario: Remove non-member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is executed and Pedro is not a member
- **THEN** an error is reported and membership is unchanged

### Requirement: GROUP DELETE command
`GROUP DELETE;<groupId>` SHALL delete the group without deleting its member users. It SHALL be rejected if the group does not exist or is locked.

#### Scenario: Delete unlocked group
- **WHEN** unlocked group `G01` with member Ana exists and `GROUP DELETE;G01` is executed
- **THEN** `G01` no longer exists and Ana is still registered

### Requirement: Group persistence
Groups, including their identifier, name and member references, SHALL be persisted after every successful change and restored at startup, with each member resolved to the restored registered user and no duplicated users.

#### Scenario: Restore membership
- **WHEN** Ana and Pedro are added to `G01` and the application restarts
- **THEN** `G01` contains exactly the restored users Ana and Pedro

#### Scenario: Persisted removal and deletion
- **WHEN** a member is removed or a group is deleted and the application restarts
- **THEN** the restored groups reflect the removal or deletion
