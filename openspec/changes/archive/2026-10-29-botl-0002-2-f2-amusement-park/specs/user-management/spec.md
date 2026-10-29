# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
`USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, check the password, delete the user and update persistent information. A nonexistent user or an incorrect password MUST NOT delete any user. Deletion MUST also be rejected while the user has an active ticket (individual or part of a group ticket) or belongs to a group locked by an active group ticket; when allowed, the user is removed from all their groups and their historical tickets are retained.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and Pedro exists with that password
- **THEN** Pedro is removed, the persistent data no longer contains Pedro and `USER DELETED` is shown

#### Scenario: Incorrect password
- **WHEN** USER DELETE is processed for an existing email with a wrong password
- **THEN** the user is not deleted and an error is shown

#### Scenario: Unknown email
- **WHEN** USER DELETE is processed for an email that no user has
- **THEN** nothing is deleted and an error is shown

#### Scenario: Active individual ticket blocks deletion
- **WHEN** Ana has a ticket dated on or after the current system date
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Active group ticket composition blocks deletion
- **WHEN** Ana is in the composition of a group ticket dated on or after the current system date
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Locked group blocks deletion
- **WHEN** Ana belongs to a group with an active group ticket and Ana has no ticket of her own
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Historical tickets do not block
- **WHEN** Ana has only tickets dated before the current date and belongs to a modifiable group
- **THEN** Ana is deleted, removed from the group, and her historical tickets remain
