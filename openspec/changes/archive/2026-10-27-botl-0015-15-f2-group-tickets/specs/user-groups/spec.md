# Spec Delta

## MODIFIED Requirements

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an existing group only when it has no associated group ticket dated on or after the current system date; an unknown identifier SHALL be rejected. Historical group tickets of a deleted group SHALL be kept, with their group reference cleared.

#### Scenario: Delete a group
- **WHEN** group `G01` exists and `GROUP DELETE;G01` is processed
- **THEN** the response is `GROUP DELETED` and `G01` no longer exists, while its former members remain registered users

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an error is reported

#### Scenario: Group with an active group ticket
- **WHEN** `G01` has a group ticket dated today or later
- **THEN** `GROUP DELETE;G01` reports an error and the group remains

#### Scenario: Group with only historical tickets
- **WHEN** `G01`'s only group ticket `TG01` is dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** the group is deleted and `TG01` still exists with no group reference, keeping its identifier, date, price and composition

## ADDED Requirements

### Requirement: Locked groups cannot change composition
GROUP ADD and GROUP REMOVE SHALL be rejected while the group has an associated group ticket dated on or after the current system date.

#### Scenario: Add to a locked group
- **WHEN** `G01` has an active group ticket and `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** an error is reported and the membership is unchanged

#### Scenario: Remove from a locked group
- **WHEN** `G01` has an active group ticket and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error is reported and Ana remains a member
