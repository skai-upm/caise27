# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly after the current system date, deleting together with it every SpeedPass and Special Pass attached to it regardless of the purchase they came from; tickets for today or earlier, group tickets and individual tickets that compose a group ticket SHALL NOT be deleted by this command.

#### Scenario: Delete future ticket
- **WHEN** T002 is dated 2027-04-20, the system date is 2027-04-15 and `TICKET DELETE;T002` is processed
- **THEN** T002 no longer exists and `TICKET DELETED` is printed

#### Scenario: Today's ticket
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated today
- **THEN** an error is printed and T001 is kept

#### Scenario: Group component
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the group ticket is unchanged

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T002 has SpeedPasses from two purchases and a Special Pass and `TICKET DELETE;T002` is processed
- **THEN** T002 and all those passes no longer exist

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL, only when the group ticket's date is strictly after the current system date, delete as one consistent operation every pass attached to its composing tickets, the composing individual tickets and the group ticket itself.

#### Scenario: Delete future group ticket
- **WHEN** TG02 is dated after today and `GROUPTICKET DELETE;TG02` is processed
- **THEN** TG02 and its composing tickets no longer exist and `GROUP TICKET DELETED` is printed

#### Scenario: Today's group ticket
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated today
- **THEN** an error is printed and TG01 is kept

#### Scenario: Members' passes deleted with the group ticket
- **WHEN** composing tickets of future group ticket TG02 have SpeedPasses and a Special Pass and `GROUPTICKET DELETE;TG02` is processed
- **THEN** those passes no longer exist
