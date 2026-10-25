# Spec Delta

## MODIFIED Requirements

### Requirement: Delete individual ticket
`TICKET DELETE;Id` SHALL delete an individual ticket only if its date is strictly later than the current date, cascade-deleting all its SpeedPasses and its Special Pass. Tickets dated today or in the past, unknown tickets, and individual tickets that belong to a group ticket SHALL NOT be deleted, and a rejected deletion SHALL leave the ticket and its passes intact.

#### Scenario: Future ticket
- **WHEN** the date is 2026-10-01 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** the ticket is deleted

#### Scenario: Ticket for today
- **WHEN** the date is 2026-10-15 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** an error is reported and the ticket remains

#### Scenario: Passes cascade
- **WHEN** a future ticket with SpeedPasses and a Special Pass is deleted
- **THEN** its passes no longer exist, also after a restart

### Requirement: Delete group ticket
`GROUPTICKET DELETE;Id` SHALL, only if its date is strictly later than the current date, delete first the passes of each of its internal individual tickets, then those internal tickets, and finally the group ticket. A rejected deletion SHALL leave everything intact.

#### Scenario: Future group ticket
- **WHEN** the date is 2026-10-01 and a group ticket dated 2026-10-15 is deleted
- **THEN** the group ticket, its internal tickets and their passes no longer exist

#### Scenario: Group ticket for today
- **WHEN** the date equals the group ticket date
- **THEN** deletion is rejected and the group ticket, internal tickets and passes remain
