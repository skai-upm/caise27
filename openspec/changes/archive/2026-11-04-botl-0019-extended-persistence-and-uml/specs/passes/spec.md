## MODIFIED Requirements

### Requirement: Ticket deletion removes its passes
When an individual ticket is deleted — directly or as a constituent of a deleted group ticket — its passes SHALL be deleted with it: for `TICKET DELETE` the ticket's passes first and then the ticket; for `GROUPTICKET DELETE` the passes of every constituent first, then the constituent tickets, then the group ticket. The deletion SHALL be validated completely before anything is removed, and if any step fails no partial deletion SHALL remain.

#### Scenario: Group ticket deleted with passes
- **GIVEN** future `TG01` and SpeedPass `P05-1` on `TG01-1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `P05-1` no longer exists

#### Scenario: Individual cascade
- **GIVEN** future ticket `T001` with SpeedPasses `P02-1` … `P02-10` and Special Pass `S01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001` and its eleven passes no longer exist, also after a restart

#### Scenario: Failed validation deletes nothing
- **GIVEN** ticket `T001` dated today with passes
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` response is printed and the ticket and all its passes are kept

#### Scenario: Failed write leaves no partial cascade
- **GIVEN** a future ticket with passes and the ticket file cannot be written
- **WHEN** the ticket deletion is attempted
- **THEN** an `ERROR:` response is printed and both the ticket and its passes still exist
