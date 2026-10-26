# Spec Delta

## MODIFIED Requirements

### Requirement: Delete a future group ticket
The system SHALL delete a group ticket with `GROUPTICKET DELETE;Id` only when its date is strictly later than the system date. The deletion SHALL first delete the passes of each internal individual ticket, then those individual tickets, and finally the group ticket; otherwise an error SHALL be reported and nothing deleted.

#### Scenario: Future group ticket
- **WHEN** the system date is before the ticket date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and its individual tickets no longer exist

#### Scenario: Current or past group ticket
- **WHEN** the system date is on or after the ticket date
- **THEN** the deletion is rejected and all tickets remain

#### Scenario: Cascade to member passes
- **WHEN** internal tickets TG01-1 and TG01-2 have SpeedPasses and `GROUPTICKET DELETE;TG01` is processed before its date
- **THEN** the passes, TG01-1, TG01-2 and TG01 no longer exist, also after a restart

## ADDED Requirements

### Requirement: Atomic ticket deletions
A ticket or group-ticket deletion SHALL either complete entirely or leave tickets and passes exactly as they were, including when storing the result fails.

#### Scenario: Storage failure
- **WHEN** storing the tickets fails during `GROUPTICKET DELETE;TG01`
- **THEN** an error is reported and TG01, its internal tickets and their passes are still present
