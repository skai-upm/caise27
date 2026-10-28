## MODIFIED Requirements

### Requirement: TICKET DELETE command
The system SHALL accept `TICKET DELETE;<identifier>` and delete the individual ticket, together with all SpeedPasses and the Special Pass associated with it, only when its date is strictly later than the current system date. Tickets for the current day or earlier SHALL be retained and the deletion rejected with an error message, as SHALL deletion of an unknown identifier or of an individual ticket belonging to a group ticket's composition.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-01, ticket `T001` is dated 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** ticket `T001` no longer exists

#### Scenario: Current-day ticket cannot be deleted
- **WHEN** the system date is 2027-04-15, ticket `T001` is dated 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** an error message is displayed and ticket `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;NOPE` is processed and no such ticket exists
- **THEN** an error message is displayed

#### Scenario: Passes deleted with their ticket
- **WHEN** future ticket `T001` has SpeedPasses from two purchases and a Special Pass, and `TICKET DELETE;T001` is processed
- **THEN** the ticket and all those passes no longer exist
