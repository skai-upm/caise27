# Spec Delta

## MODIFIED Requirements

### Requirement: Delete group
`GROUP DELETE;Identifier` SHALL delete the group only when it has no associated group ticket dated on or after the current system date; otherwise, or when the group does not exist, it MUST be rejected with an error and the group kept. Historical group tickets of a deleted group MUST be retained, with their group reference cleared and their identifier, date, price and composition intact.

#### Scenario: Group without active tickets deleted
- **GIVEN** group `G01` with no active group ticket
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** a confirmation is printed and `G01` no longer exists

#### Scenario: Group with an active ticket kept
- **GIVEN** group `G01` has a group ticket for the current day
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error reporting the active group ticket is printed and `G01` still exists

#### Scenario: Historical group ticket retained
- **GIVEN** group `G01` whose only group ticket `TG01` is historical
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `G01` is deleted and `TG01` still exists with its identifier, date, price and composition and no group reference
