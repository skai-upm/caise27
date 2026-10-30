# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket, together with all its SpeedPasses and its Special Pass, only when its date is strictly after the current system date. Tickets for the current day or earlier SHALL be kept. Individual tickets composing a group ticket, and group tickets, SHALL NOT be deleted with this command.

#### Scenario: Future ticket deleted
- **WHEN** T001 is dated after the current date and `TICKET DELETE;T001` is processed
- **THEN** T001 no longer exists

#### Scenario: Current-day ticket kept
- **WHEN** T001 is dated on the current date and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` message is printed and T001 still exists

#### Scenario: Composed ticket protected
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket composing group ticket TG01
- **THEN** an `ERROR:` message is printed and the ticket still exists

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T001 has SpeedPasses from two purchases and a Special Pass and `TICKET DELETE;T001` is processed
- **THEN** T001 and all those passes no longer exist

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete, only when the group ticket's date is strictly after the current system date, the passes of its composed tickets, the composed tickets and the group ticket itself, as one operation; otherwise everything SHALL be kept as a historical record.

#### Scenario: Future group ticket deleted with composition
- **WHEN** TG01 is dated after the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01, all `TG01:<dni>` tickets and their passes no longer exist

#### Scenario: Current-day group ticket kept
- **WHEN** TG01 is dated on the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an `ERROR:` message is printed and TG01 still exists
