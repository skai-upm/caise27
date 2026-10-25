# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket, together with all its SpeedPasses and its Special Pass, only when its date is strictly after the current system date. Tickets for the current day or earlier SHALL be kept as historical records. Individual tickets that belong to a group ticket SHALL NOT be deletable with `TICKET DELETE`.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** T002 is deleted

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated the current system date
- **THEN** an error is printed and T001 is kept

#### Scenario: Component of a group ticket
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed
- **THEN** an error is printed and the group ticket is unchanged

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T002 has SpeedPasses and a Special Pass and `TICKET DELETE;T002` succeeds
- **THEN** none of those passes exist afterwards

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket, together with its individual tickets and all passes of those individual tickets, only when its date is strictly after the current system date. A rejected deletion SHALL change nothing.

#### Scenario: Delete future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** TG02 and its individual tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated the current system date
- **THEN** an error is printed and TG01 is kept

#### Scenario: Passes deleted with the group ticket
- **WHEN** TG02's individual tickets have SpeedPasses and `GROUPTICKET DELETE;TG02` succeeds
- **THEN** those SpeedPasses no longer exist
