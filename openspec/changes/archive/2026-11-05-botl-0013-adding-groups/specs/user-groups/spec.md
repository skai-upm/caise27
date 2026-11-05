# Spec Delta

## Purpose

Defines user groups: identified, named collections of registered users that can be created empty and whose membership can be modified, as the basis for group tickets.

## ADDED Requirements

### Requirement: GROUP CREATE command
`GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name and print `GROUP CREATED`. Group identifiers SHALL be unique; an existing identifier or an empty identifier or name MUST be rejected with an error message starting with `ERROR`.

#### Scenario: Empty group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** `GROUP CREATED` is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an error message starting with `ERROR` is printed and `G01` keeps the name `Familia Garcia`

### Requirement: GROUP ADD command
`GROUP ADD;Identifier;Email` SHALL add the registered user with that email to the group and print `USER ADDED TO GROUP`. It MUST be rejected with an error message starting with `ERROR`, leaving the group unchanged, when the group does not exist, no user has that email, or the user already belongs to that group.

#### Scenario: Users added progressively
- **GIVEN** an empty group `G01` and registered users `ana@correo.com` and `pedro@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both print `USER ADDED TO GROUP` and `G01` has the two users as members

#### Scenario: User cannot be added twice
- **GIVEN** `ana@correo.com` belongs to `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana appears once in `G01`

#### Scenario: Unknown group or user rejected
- **WHEN** `GROUP ADD;G99;ana@correo.com` or `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and no membership changes

#### Scenario: Same user in different groups
- **GIVEN** groups `G01` and `G02` and Ana a member of `G01`
- **WHEN** `GROUP ADD;G02;ana@correo.com` is processed
- **THEN** `USER ADDED TO GROUP` is printed

### Requirement: GROUP REMOVE command
`GROUP REMOVE;Identifier;Email` SHALL remove the user with that email from the group and print `USER REMOVED FROM GROUP`. It MUST be rejected with an error message starting with `ERROR` when the group does not exist or the user is not a member of it.

#### Scenario: Member removed
- **GIVEN** Ana belongs to `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `USER REMOVED FROM GROUP` is printed and Ana is no longer a member of `G01`

#### Scenario: Non-member removal rejected
- **GIVEN** Pedro does not belong to `G01`
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed
