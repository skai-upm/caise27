# Spec Delta

## MODIFIED Requirements

### Requirement: Delete an individual ticket
The system SHALL delete an individual ticket with `TICKET DELETE;<identifier>` only when it exists, is not part of a group ticket, and its date is strictly later than the current system date. Tickets dated on or before the current system date SHALL be kept as historical records and the deletion rejected with an error message. Individual tickets that compose a group ticket SHALL only be deleted through deletion of their group ticket. When the deletion is valid, every SpeedPass and Special Pass associated with the ticket SHALL be deleted with it, as a joint refund.

#### Scenario: Delete a future ticket
- **GIVEN** the system date is 2027-04-15 and ticket `T002` is dated 2027-04-16
- **WHEN** `TICKET DELETE;T002` is processed
- **THEN** a confirmation is displayed and `T002` no longer exists

#### Scenario: Ticket for today or earlier cannot be deleted
- **GIVEN** the system date is 2027-04-15 and ticket `T001` is dated 2027-04-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error message is displayed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no ticket `T999` exists
- **THEN** an error message is displayed

#### Scenario: Composed ticket cannot be deleted directly
- **GIVEN** individual ticket `TG01:12345678Z` is part of future group ticket `TG01`
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error message is displayed and the ticket and its group ticket remain

#### Scenario: Passes deleted with their ticket
- **GIVEN** future ticket `T002` has SpeedPasses from two purchases and a Special Pass
- **WHEN** `TICKET DELETE;T002` is processed
- **THEN** `T002` and all its passes no longer exist

### Requirement: Delete a group ticket
The system SHALL delete a group ticket with `GROUPTICKET DELETE;<identifier>` only when it exists and is dated strictly after the current system date. It SHALL then delete all SpeedPasses and Special Passes of its composed individual tickets, the composed individual tickets, and the group ticket itself, completely or not at all. Group tickets for the current day or earlier SHALL be kept as historical records and the deletion rejected with an error message.

#### Scenario: Delete a future group ticket
- **GIVEN** system date 2027-04-15 and group ticket `TG01` dated 2027-04-20
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** a confirmation is displayed and `TG01` and all its composed tickets no longer exist
- **AND** if no other active group ticket remains for its group, the group can be modified again

#### Scenario: Today's group ticket cannot be deleted
- **GIVEN** group ticket `TG01` is dated today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is displayed and `TG01` and its composed tickets remain

#### Scenario: Passes of composed tickets deleted with the group ticket
- **GIVEN** future group ticket `TG01` whose composed ticket `TG01:12345678Z` has a SpeedPass and a Special Pass
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** those passes, the composed tickets and `TG01` no longer exist
