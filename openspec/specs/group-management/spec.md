# group-management Specification

## Purpose

Organizes existing users into named groups that can later buy group tickets, keeping references to the registered users rather than copies.

## Requirements

### Requirement: Group model
A group SHALL have a unique identifier, a name, and a collection of references to registered users. Groups SHALL NOT create copies of users.

#### Scenario: Member is the registered user
- **GIVEN** user `ana@correo.com` is in group `G01`
- **WHEN** Ana's name is changed with `USER UPDATE`
- **THEN** the group member reflects the new name

### Requirement: Create and delete groups
`GROUP CREATE;<id>;<name>` SHALL create an empty group with a unique identifier and non-empty name. `GROUP DELETE;<id>` SHALL delete an existing group unless the group is locked.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otra` is executed
- **THEN** an error is reported and the existing group is unchanged

#### Scenario: Delete unknown group
- **WHEN** `GROUP DELETE;G99` is executed and `G99` does not exist
- **THEN** an error is reported

### Requirement: Group membership
`GROUP ADD;<id>;<email>` SHALL add the registered user with that email to the group. `GROUP REMOVE;<id>;<email>` SHALL remove a member. Adding an unknown user, adding a user already in the group, or removing a non-member SHALL be rejected without changing membership.

#### Scenario: Add and remove member
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` then `GROUP REMOVE;G01;ana@correo.com` are executed
- **THEN** Ana is a member after the first command and not after the second

#### Scenario: Invalid membership
- **GIVEN** group `G01` already containing Ana
- **WHEN** `GROUP ADD;G01;nadie@correo.com` or `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** an error is reported and membership is unchanged

### Requirement: Group locking by active group tickets
A group that has at least one group ticket dated on or after the system date SHALL be locked: `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it SHALL be rejected. The lock SHALL be evaluated against the current system date and not stored as a flag.

#### Scenario: Locked group
- **GIVEN** system date 2026-10-01 and a group ticket for `G01` dated 2026-10-15
- **WHEN** `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is executed on `G01`
- **THEN** each is rejected and the group is unchanged

#### Scenario: Unlocked after date advances
- **GIVEN** the group ticket above
- **WHEN** `SYSTEM DATE;2026-10-16` is executed and then a member is removed
- **THEN** the removal succeeds

### Requirement: Group persistence
Groups and their membership SHALL be stored in a text file and restored at startup, with members resolved to the restored registered users.

#### Scenario: Restore membership
- **GIVEN** a group with two members was saved
- **WHEN** the application restarts
- **THEN** the group contains references to the same two restored users and no duplicated users exist
