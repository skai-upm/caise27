# Spec Delta

## REMOVED Requirements

### Requirement: Tickets with passes are not deleted
**Reason**: Replaced by cascade deletion of passes together with their tickets.
**Migration**: `TICKET DELETE` and `GROUPTICKET DELETE` now remove the passes automatically; no refund step is needed first.

## ADDED Requirements

### Requirement: Cascade deletion of passes
When a ticket is validly deleted, its passes SHALL be deleted with it: `TICKET DELETE` SHALL delete the ticket's SpeedPasses and Special Pass, and `GROUPTICKET DELETE` SHALL delete the passes of each composing ticket, then those tickets, then the group ticket. All deletion conditions SHALL be checked before anything is removed, so a rejected deletion SHALL leave every ticket and pass unchanged.

#### Scenario: Individual ticket with passes
- **WHEN** the system date is 2026-10-01, `T001` (dated 2026-10-15) has SpeedPasses and a Special Pass, and `TICKET DELETE;T001` is processed
- **THEN** `T001` and all its passes no longer exist, also after a restart

#### Scenario: Group ticket with passes
- **WHEN** `TG01` is dated after the system date, `TG01-1` has a SpeedPass and `GROUPTICKET DELETE;TG01` is processed
- **THEN** the SpeedPass, `TG01-1`, `TG01-2` and `TG01` no longer exist

#### Scenario: Rejected deletion
- **WHEN** `TICKET DELETE;T001` is processed on the ticket's date
- **THEN** it is rejected and `T001` keeps all its passes
