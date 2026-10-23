# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly after the current system date. With the ticket it SHALL delete all its SpeedPasses and its Special Pass, consumed or not, persist the change atomically, and print `TICKET DELETED`. Tickets dated today or earlier, group tickets, and individual tickets inside a group ticket SHALL be rejected with an error, leaving everything unchanged.

#### Scenario: Delete future ticket
- **WHEN** the system date is `2027-04-15` and `TICKET DELETE;T002` is processed for a ticket dated `2027-04-20`
- **THEN** `TICKET DELETED` is printed and `T002` no longer exists

#### Scenario: Today's ticket kept
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated on the current system date
- **THEN** an error is printed and `T001` still exists

#### Scenario: Group component cannot be deleted directly
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed for an individual ticket inside group ticket `TG02`
- **THEN** an error is printed and the ticket still exists

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket `T002` has SpeedPasses from two purchases and a Special Pass, and `TICKET DELETE;T002` is processed
- **THEN** the ticket and all of those passes no longer exist, including after a restart
