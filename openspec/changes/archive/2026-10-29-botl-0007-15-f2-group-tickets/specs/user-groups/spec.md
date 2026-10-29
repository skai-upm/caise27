# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group
The command `GROUP DELETE;Identifier` SHALL delete the group. Deletion SHALL be rejected with an error message when the group does not exist or when an associated group ticket has a date equal to or later than the current system date; group tickets for earlier dates SHALL NOT prevent deletion. When a group is deleted, its historical group tickets SHALL be kept with their identifier, date, price and composition, and their group reference SHALL become null.

#### Scenario: Delete a group without tickets
- **WHEN** group `G01` has no associated group tickets and `GROUP DELETE;G01` is processed
- **THEN** a confirmation is shown and `G01` no longer exists

#### Scenario: Delete unknown group rejected
- **WHEN** `GROUP DELETE;G99` is processed and no group `G99` exists
- **THEN** an error message is shown

#### Scenario: Active group ticket blocks deletion
- **WHEN** group `G01` has a group ticket dated on the current date or later and `GROUP DELETE;G01` is processed
- **THEN** an error message is shown and `G01` still exists

#### Scenario: Historical group tickets kept
- **WHEN** group `G01` only has historical group tickets and `GROUP DELETE;G01` is processed
- **THEN** `G01` is deleted and its group tickets remain with a null group reference and unchanged composition and price
