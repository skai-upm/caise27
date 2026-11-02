# group-management Specification

## Purpose

Lets registered users be organized into named groups that reference the existing user records and survive restarts.

## Requirements

### Requirement: Create group
`GROUP CREATE;<groupId>;<name>` SHALL create an empty group with a unique, non-empty identifier and a non-empty name. A duplicate identifier or empty value SHALL be rejected.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed and no group `G01` exists
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate group identifier
- **WHEN** `GROUP CREATE;G01;Otro` is executed and group `G01` exists
- **THEN** an error is shown and the existing group is unchanged

### Requirement: Group membership references existing users
`GROUP ADD;<groupId>;<email>` SHALL add the registered user with that email to the group as a reference to the same user record, not a copy. Adding to an unknown group, adding an unregistered email, or adding a user already in the group SHALL be rejected without changing membership.

#### Scenario: Add existing user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is executed for registered user Ana
- **THEN** Ana is a member of `G01`, and later changes to Ana's data are visible through the group

#### Scenario: Invalid membership
- **WHEN** an unregistered email or an email already in the group is added
- **THEN** an error is shown and group membership is unchanged

### Requirement: Remove member and delete group
`GROUP REMOVE;<groupId>;<email>` SHALL remove that member from the group, and `GROUP DELETE;<groupId>` SHALL delete the group without affecting its users. An unknown group, or removing a user who is not a member, SHALL be rejected without changes.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is executed and Ana is a member
- **THEN** Ana is no longer a member and remains a registered user

#### Scenario: Delete group
- **WHEN** `GROUP DELETE;G01` is executed for an unlocked group
- **THEN** the group no longer exists and its former members remain registered users

### Requirement: Locked group
A group that has at least one group ticket dated on or after the system date SHALL reject `GROUP ADD`, `GROUP REMOVE`, and `GROUP DELETE`. The lock SHALL be evaluated against the current system date at each command, so it ends once all of the group's tickets are in the past.

#### Scenario: Locked and unlocked group
- **WHEN** group `G01` has a group ticket dated 2026-10-15, the system date is 2026-10-15, and `GROUP ADD`, `GROUP REMOVE`, or `GROUP DELETE` is executed for `G01`
- **THEN** each request is rejected; after `SYSTEM DATE;2026-10-16` the same requests are allowed

### Requirement: Group persistence
Groups, their names, and their membership SHALL be persisted after every successful change and restored at startup with members linked to the restored user records, without duplicating users.

#### Scenario: Restore membership
- **WHEN** existing users were added to a group and the application restarts in normal mode
- **THEN** the group contains references to the restored users and each user exists exactly once
