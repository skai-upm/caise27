# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket, together with all SpeedPasses and the Special Pass associated with it, only when its date is strictly later than the current system date. Tickets for the current day or earlier MUST be kept as historical records, and individual tickets that belong to a group ticket MUST NOT be deleted with this command.

#### Scenario: Delete future ticket
- **WHEN** the date is 2027-04-15 and `TICKET DELETE;T005` is processed for a ticket dated 2027-04-20
- **THEN** T005 and its passes are deleted

#### Scenario: Today's ticket not deletable
- **WHEN** the date is 2027-04-15 and `TICKET DELETE;T001` is processed for a ticket dated 2027-04-15
- **THEN** an error is shown and T001 remains

#### Scenario: Composition ticket not deletable
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is shown and the group ticket is unchanged

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete the passes of every composition ticket, the composition tickets and the group ticket only when its date is strictly later than the current system date, without partial deletion.

#### Scenario: Delete future group ticket
- **WHEN** TG01 is dated after the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01, its composition tickets and their passes are deleted

#### Scenario: Today's group ticket not deletable
- **WHEN** TG01 is dated on the current date
- **THEN** `GROUPTICKET DELETE;TG01` is rejected
