# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
The system SHALL accept `USER DELETE;Email;Password`, locate the user by email, verify it exists, check the password, delete the user and update persistent data, printing `USER DELETED` on success. Deletion SHALL be rejected while the user holds any active ticket (individual or composing a group ticket) or belongs to a locked group; otherwise the user is also removed from all their groups. Historical tickets SHALL be kept.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, also after restart

#### Scenario: Unknown email
- **WHEN** USER DELETE is processed with an email that no user has
- **THEN** an `ERROR:` message is printed and no user is deleted

#### Scenario: Incorrect password
- **WHEN** USER DELETE is processed with an existing email and a wrong password
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has an individual ticket, or a ticket composing a group ticket, dated on or after the current system date
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket not including the user
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Deletion removes group membership and keeps history
- **WHEN** the user only has historical tickets and belongs only to unlocked groups
- **THEN** the user is deleted, removed from those groups, and the historical tickets still exist referring to the former user
