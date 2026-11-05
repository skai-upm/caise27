## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete the individual ticket, together with every SpeedPass and the Special Pass associated with it (whatever operation purchased them), and print a response starting with `OK` only when its date is strictly later than the current system date. Tickets for the current day or earlier MUST NOT be deleted and are retained as historical records. An unknown identifier, or a ticket that belongs to the composition of a group ticket, MUST be rejected with an `ERROR` response.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2027-04-01 and ticket `T001` is dated 2027-04-15
- **THEN** `TICKET DELETE;T001` succeeds and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **WHEN** ticket `T001` is dated 2027-04-15 and the system date is 2027-04-15 or later
- **THEN** `TICKET DELETE;T001` is rejected with an `ERROR` response and the ticket still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an `ERROR` response is printed

#### Scenario: Future ticket deleted together with its passes
- **GIVEN** the system date is 2027-04-01, ticket `T001` is dated 2027-04-15 and has SpeedPasses `SP01:1`, `SP02:1` to `SP02:10` and Special Pass `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the response starts with `OK`, and `T001` and all of its passes no longer exist
