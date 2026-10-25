## MODIFIED Requirements

### Requirement: Delete individual ticket
`TICKET DELETE;Id` SHALL delete an individual ticket only when its date is strictly after the system date. Tickets dated on or before the system date, unknown identifiers, and tickets generated inside a group ticket SHALL be rejected. Deleting a ticket SHALL also delete all its SpeedPasses and its Special Pass; a rejected deletion SHALL delete nothing.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** T001 is deleted

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** an error is shown and T001 remains

#### Scenario: Passes cascade
- **WHEN** T001 has SpeedPasses and a Special Pass and is deleted
- **THEN** its passes no longer exist, also after a restart

### Requirement: Delete group ticket
`GROUPTICKET DELETE;Id` SHALL delete a group ticket and all its generated individual tickets only when its date is strictly after the system date. Otherwise, or for unknown identifiers, it SHALL be rejected. The passes of each generated ticket SHALL be deleted first, then the generated tickets, then the group ticket; a rejected deletion SHALL delete nothing.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is 2026-10-01 and `GROUPTICKET DELETE;TG01` is executed for a group ticket dated 2026-10-15
- **THEN** TG01 and its individual tickets no longer exist

#### Scenario: Member passes cascade
- **WHEN** a generated ticket of TG01 has passes and TG01 is deleted
- **THEN** those passes no longer exist

#### Scenario: Rejected deletion keeps everything
- **WHEN** `GROUPTICKET DELETE;TG01` is rejected because TG01 is dated on the system date
- **THEN** TG01, its generated tickets and their passes all remain
