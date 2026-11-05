## ADDED Requirements

### Requirement: Ticket deletion removes associated passes
When a future individual ticket is deleted with `TICKET DELETE`, all SpeedPasses and the Special Pass associated with it SHALL be deleted too, whatever purchase created them. When a future group ticket is deleted with `GROUPTICKET DELETE`, the passes of every composition ticket, the composition tickets and the group ticket SHALL be deleted together; if the deletion is not allowed nothing is removed.

#### Scenario: Individual ticket with passes deleted
- **GIVEN** future ticket `T001` with `SP01:1`, `SP02:1`…`SP02:10` and `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `TICKET DELETED` is printed and none of those passes exists

#### Scenario: Group ticket deletion with member passes
- **GIVEN** future group ticket `TG01` whose composition ticket `TG01:12345678Z` has SpeedPass `SP03:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP03:1`, the composition tickets and `TG01` no longer exist

#### Scenario: Rejected deletion keeps passes
- **GIVEN** ticket `T001` dated today with SpeedPass `SP01:1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `SP01:1` still exists
