# Spec Delta

## ADDED Requirements

### Requirement: Active tickets block user deletion
`USER DELETE` SHALL be rejected while any active ticket (dated on or after the current system date) is associated with the user, whether a standalone individual ticket or an individual ticket composing a group ticket. Historical tickets alone SHALL NOT block deletion. The lock SHALL be derived from the tickets and the current date, not stored on the user.

#### Scenario: Ticket for today blocks deletion
- **WHEN** Ana has an individual ticket dated today and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and Ana still exists

#### Scenario: Group composition ticket blocks deletion
- **WHEN** Ana is in the composition of a future group ticket and her deletion is requested
- **THEN** deletion is rejected

#### Scenario: Historical tickets only
- **WHEN** all of Ana's tickets are dated before the current system date
- **THEN** Ana can be deleted and her historical tickets are retained

### Requirement: Locked groups block user deletion
`USER DELETE` SHALL be rejected when the user belongs to a group locked by an active group ticket. When deletion is permitted, the user SHALL also be removed from every group they belong to.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to a group with an active group ticket
- **THEN** her deletion is rejected and the group is unchanged

#### Scenario: Removal from modifiable groups
- **WHEN** Ana belongs only to unlocked groups and is deleted
- **THEN** she is no longer a member of any group
