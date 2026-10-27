# Spec Delta

## MODIFIED Requirements

### Requirement: Group modifiability derived from tickets
A group SHALL be modifiable (GROUP ADD, GROUP REMOVE) and deletable (GROUP DELETE) only when it has no associated group ticket dated on or after the current system date. This locked state SHALL be computed from the existing tickets and the current system date and SHALL NOT be stored as a group attribute.

#### Scenario: Group without tickets is modifiable
- **WHEN** group `G01` has no associated group tickets
- **THEN** GROUP ADD, GROUP REMOVE and GROUP DELETE are allowed

#### Scenario: Active group ticket locks the group
- **WHEN** group `G01` has group ticket `TG01` dated `2027-04-15` and the system date is `2027-04-15`
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are rejected with an error

#### Scenario: Deleting the future ticket unlocks the group
- **WHEN** the system date is `2027-04-01`, `G01`'s only group ticket `TG01` is dated `2027-04-15` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `GROUP ADD` and `GROUP REMOVE` on `G01` are allowed again

#### Scenario: Historical tickets do not lock
- **WHEN** `G01`'s only group ticket is dated `2027-04-15` and the system date is `2027-04-16`
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are allowed

## ADDED Requirements

### Requirement: Historical group tickets survive group deletion
When a group is deleted, its historical group tickets SHALL be kept with their identifier, date, price and composition, and their reference to the deleted group SHALL become empty.

#### Scenario: Group deleted after its ticket date
- **WHEN** `TG01` for `G01` is dated `2027-04-15`, the system date is `2027-04-16` and `GROUP DELETE;G01` is processed
- **THEN** `G01` is deleted and `TG01` still exists with its date, price and composition and no group reference
