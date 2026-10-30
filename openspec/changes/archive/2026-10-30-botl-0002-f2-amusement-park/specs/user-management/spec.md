## MODIFIED Requirements

### Requirement: Delete user
`USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, verify the password, delete the user, update persistent data and print `USER DELETED`. An unknown email or incorrect password SHALL leave the user in place and print an error. Deletion SHALL also be rejected while the user has an active ticket (individual or inside a group ticket) or belongs to a group locked by an active group ticket; otherwise the user SHALL be removed from all groups.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, also after restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** an error is printed and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email of no existing user
- **THEN** an error is printed and no user changes

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has an individual ticket, or a ticket inside a group ticket, dated today or later
- **THEN** an error is printed and the user still exists

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket
- **THEN** an error is printed and the user still exists

#### Scenario: Removal from groups
- **WHEN** the user has only historical tickets and belongs to unlocked groups
- **THEN** the user is deleted and no longer a member of those groups, and historical tickets are kept
