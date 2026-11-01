# Spec Delta

## MODIFIED Requirements

### Requirement: GROUPTICKET DELETE command
`GROUPTICKET DELETE;<Id>` SHALL delete a group ticket only when its date is strictly later than the system date, removing first the passes of each constituent ticket, then the constituent tickets, and finally the group ticket, as one all-or-nothing operation; otherwise, or for unknown identifiers, it SHALL be rejected leaving every ticket and pass unchanged in memory and in storage.

#### Scenario: All-or-nothing group-ticket deletion
- **GIVEN** system date 2026-10-01 and group ticket `TG001` dated 2026-10-15 with three constituent tickets
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** the group ticket and its three constituent tickets are deleted

#### Scenario: Today or past group ticket is kept
- **GIVEN** system date 2026-10-15 and group ticket `TG01` dated 2026-10-15
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is reported and the group ticket and all its constituents remain

#### Scenario: Constituent passes removed
- **GIVEN** a future group ticket `TG01` whose constituent tickets have SpeedPasses
- **WHEN** `GROUPTICKET DELETE;TG01` succeeds and the application restarts
- **THEN** no pass of any `TG01` constituent ticket exists
