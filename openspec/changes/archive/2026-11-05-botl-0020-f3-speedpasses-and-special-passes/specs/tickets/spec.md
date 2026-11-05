# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
The command `TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets dated today or earlier SHALL be kept as historical records and the deletion rejected; unknown identifiers SHALL be rejected. Individual tickets that are part of a group ticket SHALL NOT be deleted with `TICKET DELETE`; they are deleted only with their group ticket. When a ticket is deleted, every SpeedPass and the Special Pass associated with it SHALL be deleted with it, even if they were bought in different operations.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is 2027-04-15 and ticket `T001` is dated 2027-04-20
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** a confirmation is shown and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **GIVEN** ticket `T001` dated 2027-04-15 and the system date 2027-04-15 (or later)
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is shown and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is shown

#### Scenario: Component of a group ticket
- **GIVEN** future group ticket `TG01` with component `TG01:12345678Z`
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is shown and the component still exists

#### Scenario: Passes deleted with their ticket
- **GIVEN** future ticket `T001` with SpeedPasses `SP01:1` and `SP02:1`, and Special Pass `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001`, `SP01:1`, `SP02:1` and `PE01` no longer exist

### Requirement: Group ticket deletion
The command `GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with all its component tickets and every SpeedPass and Special Pass associated with those components. All checks SHALL happen before anything is removed, so the deletion is never partial. Today's and past group tickets SHALL be kept as historical records, and unknown identifiers SHALL be rejected.

#### Scenario: Future group ticket deleted with its composition
- **GIVEN** group ticket `TG01` dated after the current system date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all its `TG01:<dni>` tickets no longer exist, and the group can be modified again if it has no other active group ticket

#### Scenario: Today's group ticket kept
- **GIVEN** group ticket `TG01` dated on the current system date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is shown and `TG01` still exists

#### Scenario: Component passes deleted with the group ticket
- **GIVEN** future group ticket `TG01`, whose component `TG01:12345678Z` has SpeedPass `SP05:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP05:1`, the components and `TG01` no longer exist
