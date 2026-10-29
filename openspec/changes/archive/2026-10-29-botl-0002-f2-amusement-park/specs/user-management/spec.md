# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify it exists, check the password, verify the user has no active ticket (standalone or inside a group ticket) and belongs to no locked group, then remove the user from all groups (and any attraction), delete the user, update persistent data and report `USER DELETED`. Historical tickets SHALL be retained.

#### Scenario: Successful deletion
- **WHEN** user `pedro@correo.com` with password `Otra2#` exists and `USER DELETE;pedro@correo.com;Otra2#` is processed
- **THEN** the output is `USER DELETED`, the user no longer exists and it is absent from the persistence file

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed for an email that no user has
- **THEN** an error is reported and no user is deleted

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and an incorrect password
- **THEN** an error is reported and the user is not deleted

#### Scenario: Active individual ticket blocks deletion
- **WHEN** Ana has an individual ticket dated on or after the system date and `USER DELETE` is processed for Ana
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Active group ticket blocks deletion
- **WHEN** Ana's only ticket is inside an active group ticket and `USER DELETE` is processed for Ana
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Member of a locked group
- **WHEN** Ana has no active ticket herself but belongs to a group locked by an active group ticket
- **THEN** `USER DELETE` for Ana is rejected

#### Scenario: Historical tickets do not block and are kept
- **WHEN** Ana has only historical tickets and belongs to an unlocked group
- **THEN** `USER DELETE` for Ana succeeds, she is removed from the group and her historical tickets still exist
