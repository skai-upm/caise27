# Spec Delta

## ADDED Requirements

### Requirement: Active group ticket locks the group
While a group has an associated group ticket dated on or after the current system date, `GROUP ADD` and `GROUP REMOVE` on it MUST be rejected with an `ERROR` response, `GROUP DELETE` MUST be rejected, and `USER DELETE` of any of its members MUST be rejected. When all its group tickets are historical or have been deleted, the group SHALL become modifiable again.

#### Scenario: Locked by a future group ticket
- **WHEN** group `G01` has a group ticket dated after the system date
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are rejected and a member's `USER DELETE` is rejected

#### Scenario: Unlocked after deleting the future group ticket
- **WHEN** the only active group ticket of `G01` is deleted with `GROUPTICKET DELETE`
- **THEN** `GROUP ADD` on `G01` succeeds

#### Scenario: Today's ticket keeps the lock, history does not
- **WHEN** `G01`'s group ticket is dated today
- **THEN** `GROUP DELETE;G01` is rejected
- **AND** once the system date moves past that date, `GROUP DELETE;G01` succeeds

### Requirement: Deleted groups keep historical group tickets
Deleting a group SHALL keep its historical group tickets as records, including identifier, date, price and composition, with their reference to the group cleared.

#### Scenario: Historical group ticket survives group deletion
- **WHEN** `G01` has only a historical group ticket `TG01` and `GROUP DELETE;G01` succeeds
- **THEN** `TG01` and its composition still exist and `TG01` no longer references a group
