# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a stand-alone individual ticket, together with all its SpeedPasses and its Special Pass, only when its date is strictly later than the system date. Tickets for the current day or earlier SHALL be kept as historical records. Individual tickets that are part of a group ticket SHALL NOT be deletable with this command.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** T002 no longer exists

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE` is processed for a ticket dated on the system date
- **THEN** an error is printed and the ticket is kept

#### Scenario: Component of a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket is kept

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T002 has SpeedPasses from two purchases and a Special Pass, and `TICKET DELETE;T002` is processed
- **THEN** T002 and all those passes no longer exist

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete, only when its date is strictly later than the system date, all passes of its individual tickets, the individual tickets, and the group ticket itself, as one operation.

#### Scenario: Delete a future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** TG02 and all `TG02:<dni>` tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE` is processed for a group ticket dated on the system date
- **THEN** an error is printed and the group ticket is kept

#### Scenario: Members' passes deleted
- **WHEN** a component of future group ticket TG02 has a SpeedPass and `GROUPTICKET DELETE;TG02` is processed
- **THEN** that SpeedPass no longer exists
