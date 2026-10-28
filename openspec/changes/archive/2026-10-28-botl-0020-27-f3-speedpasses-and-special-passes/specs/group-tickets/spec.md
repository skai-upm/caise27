## MODIFIED Requirements

### Requirement: GROUPTICKET DELETE command
The system SHALL accept `GROUPTICKET DELETE;<identifier>` and delete, as one consistent operation, all SpeedPasses and Special Passes of the individual tickets in its composition, those individual tickets and the group ticket itself, only when its date is strictly later than the current system date. Group tickets for the current day or earlier SHALL be retained and the deletion rejected with an error message, as SHALL an unknown identifier; a rejected deletion SHALL leave everything unchanged.

#### Scenario: Delete a future group ticket
- **WHEN** the system date is 2027-04-01, group ticket `TG01` is dated 2027-04-15 and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all its derived individual tickets no longer exist

#### Scenario: Current-day group ticket retained
- **WHEN** the system date is 2027-04-15, `TG01` is dated 2027-04-15 and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is displayed and `TG01` still exists

#### Scenario: Passes of the composition deleted
- **WHEN** a member's individual ticket in future group ticket `TG01` has a SpeedPass and `GROUPTICKET DELETE;TG01` is processed
- **THEN** that SpeedPass, the individual tickets and `TG01` no longer exist
