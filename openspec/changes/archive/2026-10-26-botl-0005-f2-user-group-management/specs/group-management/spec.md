# Spec Delta

## Purpose

Lets registered users be organized into named groups whose membership is kept consistent with existing users and persisted between executions.

## ADDED Requirements

### Requirement: Create group
`GROUP CREATE;<id>;<name>` SHALL create an empty group with that identifier and name and print `OK GROUP CREATED`. The identifier and name SHALL be non-empty and the identifier SHALL be unique; otherwise an `ERROR` line SHALL be printed.

#### Scenario: New group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group `G01` named `Familia Garcia` exists with no members and `OK GROUP CREATED` is printed

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** no group is created and an `ERROR` line is printed

### Requirement: Add member
`GROUP ADD;<id>;<email>` SHALL add the registered user with that email to the group and print `OK USER ADDED TO GROUP`. It SHALL fail with an `ERROR` line if the group does not exist, no user has that email, or the user is already in the group.

#### Scenario: Add existing user
- **WHEN** group `G01` exists, `ana@correo.com` is registered and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** Ana is a member of `G01`

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** the group is unchanged and an `ERROR` line is printed

#### Scenario: Duplicate member
- **WHEN** Ana is already in `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the group still contains Ana once and an `ERROR` line is printed

### Requirement: Members are the registered users
A group SHALL reference the existing user objects rather than independent copies, so changes to a user are visible through every group that contains them.

#### Scenario: Updated user seen through group
- **WHEN** Ana is in `G01` and her first name is changed with `USER UPDATE`
- **THEN** the member of `G01` shows the new first name

### Requirement: Remove member
`GROUP REMOVE;<id>;<email>` SHALL remove that user from the group and print `OK USER REMOVED FROM GROUP`; it SHALL fail with an `ERROR` line if the group does not exist or the user is not a member.

#### Scenario: Remove member
- **WHEN** Ana is in `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** Ana is no longer in `G01`

#### Scenario: Not a member
- **WHEN** Ana is not in `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an `ERROR` line is printed

### Requirement: Delete group
`GROUP DELETE;<id>` SHALL delete the group (its users remain registered) and print `OK GROUP DELETED`; it SHALL fail with an `ERROR` line if the group does not exist.

#### Scenario: Delete group
- **WHEN** `GROUP DELETE;G01` is processed for an existing group with members
- **THEN** the group no longer exists and its former members are still registered users

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in a text file after every change and restored at startup, with each member linked to the registered user with the same DNI.

#### Scenario: Restart restores groups
- **WHEN** group `G01` with Ana and Pedro exists and the application is restarted
- **THEN** `G01` exists with name `Familia Garcia` and members Ana and Pedro
