## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date and it is not part of a group ticket. Tickets for the current day or earlier SHALL be retained as historical records. Group tickets SHALL NOT be deletable with this command. From Delivery 3 a valid deletion SHALL also delete all SpeedPasses and the Special Pass associated with the ticket, in the same operation.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a standalone ticket dated 2027-04-16
- **THEN** T002 is deleted

#### Scenario: Ticket for today retained
- **WHEN** `TICKET DELETE` is processed for a ticket dated on the current system date
- **THEN** an error is reported and the ticket is retained

#### Scenario: Constituent of a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for an individual ticket composing group ticket TG01
- **THEN** an error is reported and the ticket is retained

#### Scenario: Deletion cascades to passes
- **WHEN** a future standalone ticket with SpeedPasses bought in several purchases and a Special Pass is deleted
- **THEN** the ticket and all those passes no longer exist
