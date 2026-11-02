## MODIFIED Requirements

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all individual tickets of its composition only when its date is strictly later than the current system date. Group tickets for the current day or earlier SHALL be retained as historical records. From Delivery 3 a valid deletion SHALL also delete all passes associated with the constituent tickets, as one operation without partial deletions.

#### Scenario: Future group ticket deleted
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated after the current date
- **THEN** TG01 and its constituent tickets no longer exist

#### Scenario: Group ticket for today retained
- **WHEN** `GROUPTICKET DELETE` is processed for a group ticket dated on the current date
- **THEN** an error is reported and the group ticket is retained

#### Scenario: Group deletion cascades to passes
- **WHEN** a future group ticket whose constituents hold passes is deleted
- **THEN** those passes, the constituents and the group ticket no longer exist
