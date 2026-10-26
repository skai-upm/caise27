# Spec Delta

## ADDED Requirements

### Requirement: Tickets with passes are not deleted
`TICKET DELETE` and `GROUPTICKET DELETE` SHALL be rejected while the individual ticket, or any individual ticket inside the group ticket, has passes.

#### Scenario: Ticket with a SpeedPass
- **WHEN** T001 is a future ticket with a SpeedPass and `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket and pass remain
