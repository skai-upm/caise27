## MODIFIED Requirements

### Requirement: Add member
The system SHALL process `GROUP ADD;Identificador;Correo`, adding the registered user with that email to the group, and SHALL reject it if the group does not exist, no user has that email, or the user is already a member of that group. The operation SHALL also be rejected while the group is locked by a group ticket dated on or after the system date.

#### Scenario: Add an existing user
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `OK:` response is printed and `G01` contains Ana

#### Scenario: Duplicate membership
- **GIVEN** Ana is already in `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** an `ERROR:` response is printed and Ana appears only once in `G01`

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an `ERROR:` response is printed and no membership is created

#### Scenario: Nonexistent group
- **WHEN** `GROUP ADD;G99;ana@correo.com` is processed and `G99` does not exist
- **THEN** an `ERROR:` response is printed

#### Scenario: Same user in several groups
- **GIVEN** Ana is in `G01` and group `G02` exists
- **WHEN** `GROUP ADD;G02;ana@correo.com` is processed
- **THEN** it succeeds and Ana is a member of both groups

#### Scenario: Locked group rejects additions
- **GIVEN** `G01` has a group ticket dated on or after the system date
- **WHEN** `GROUP ADD;G01;pedro@correo.com` is processed
- **THEN** an `ERROR:` response is printed and Pedro is not added

### Requirement: Remove member
The system SHALL process `GROUP REMOVE;Identificador;Correo`, removing that user from the group, and SHALL reject it if the group does not exist or the user with that email is not a member. The operation SHALL also be rejected while the group is locked by a group ticket dated on or after the system date.

#### Scenario: Remove a member
- **GIVEN** Ana is in `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an `OK:` response is printed, Ana is no longer in `G01`, and Ana is still a registered user

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not in `G01`
- **THEN** an `ERROR:` response is printed

#### Scenario: Locked group rejects removals
- **GIVEN** `G01` has a group ticket dated on or after the system date
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an `ERROR:` response is printed and Ana remains a member

### Requirement: Delete group
The system SHALL process `GROUP DELETE;Identificador`, deleting the group if it exists without deleting or changing its member users. The operation SHALL also be rejected while the group is locked by a group ticket dated on or after the system date.

#### Scenario: Delete a group
- **GIVEN** group `G01` with member Ana
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an `OK:` response is printed, `G01` no longer exists and Ana is still registered

#### Scenario: Nonexistent group
- **WHEN** `GROUP DELETE;G99` is processed and `G99` does not exist
- **THEN** an `ERROR:` response is printed

#### Scenario: Locked group cannot be deleted
- **GIVEN** `G01` has a group ticket dated on or after the system date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an `ERROR:` response is printed and `G01` still exists
