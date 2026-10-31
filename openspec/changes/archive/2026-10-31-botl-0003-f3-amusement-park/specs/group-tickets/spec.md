# Spec Delta

## MODIFIED Requirements

### Requirement: GROUPTICKET DELETE command
`GROUPTICKET DELETE;<id>` SHALL delete, only when its date is strictly later than the system date, the passes of every constituent ticket, then the constituent tickets, then the group ticket, as one operation; otherwise, or for an unknown identifier, it SHALL be rejected leaving every ticket and pass unchanged. A failure while saving SHALL leave neither memory nor files partially deleted.

#### Scenario: All-or-nothing deletion
- **WHEN** future group ticket `TG01` with two constituents is deleted
- **THEN** `TG01`, `TG01-1` and `TG01-2` no longer exist

#### Scenario: Current group ticket not deleted
- **WHEN** `TG01` is dated on the system date and `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is reported and `TG01` and its constituents remain

#### Scenario: Constituent passes removed
- **WHEN** constituents of future `TG01` hold SpeedPasses and `GROUPTICKET DELETE;TG01` succeeds
- **THEN** after a restart no pass of `TG01-1` or `TG01-2` exists
