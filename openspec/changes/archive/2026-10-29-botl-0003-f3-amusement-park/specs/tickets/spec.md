# Spec Delta

## MODIFIED Requirements

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the current system date. It SHALL NOT delete tickets that belong to a group ticket's composition, or group tickets. Deleting a ticket SHALL also delete all its SpeedPasses and its Special Pass, regardless of how they were bought.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-01 and `TICKET DELETE;T001` is processed for T001 dated 2027-04-15
- **THEN** T001 no longer exists

#### Scenario: Today's ticket kept
- **WHEN** the system date equals T001's date and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and T001 still exists

#### Scenario: Composition ticket protected
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket still exists

#### Scenario: Passes deleted with ticket
- **WHEN** future T001 has SpeedPasses SP01:1 and SP02:1..10 and Special Pass PE01, and `TICKET DELETE;T001` is processed
- **THEN** T001 and all those passes no longer exist

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and its composition tickets only when its date is strictly after the current system date. It SHALL also delete all passes of those composition tickets, consistently: either everything is deleted or nothing is.

#### Scenario: Delete future group ticket
- **WHEN** the system date is before TG01's date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Today's group ticket kept
- **WHEN** the system date equals TG01's date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and TG01 still exists

#### Scenario: Composition passes deleted
- **WHEN** a composition ticket of future TG01 has SpeedPasses and a Special Pass and `GROUPTICKET DELETE;TG01` is processed
- **THEN** those passes no longer exist
