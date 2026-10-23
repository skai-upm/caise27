# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the system date, together with all SpeedPasses and the Special Pass bought for it. Tickets dated today or earlier SHALL be kept as historical records. Individual tickets that belong to a group ticket, and group tickets themselves, SHALL NOT be deleted with this command.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** `TICKET DELETED` is printed and T002 no longer exists

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated on the system date
- **THEN** an error is printed and T001 still exists

#### Scenario: Member of a group ticket
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed
- **THEN** an error stating that it belongs to a group ticket is printed and nothing is deleted

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T002 has SpeedPasses from two purchases and a Special Pass and `TICKET DELETE;T002` succeeds
- **THEN** all those passes no longer exist

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket, together with all its member tickets and all passes bought for those member tickets, only when its date is strictly after the system date. If it cannot be deleted, nothing SHALL be deleted.

#### Scenario: Delete future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** `GROUP TICKET DELETED` is printed and TG02 and its member tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated on the system date
- **THEN** an error is printed and TG01 still exists

#### Scenario: Member passes deleted
- **WHEN** a member ticket of future group ticket TG02 has a SpeedPass and TG02 is deleted
- **THEN** that SpeedPass no longer exists
