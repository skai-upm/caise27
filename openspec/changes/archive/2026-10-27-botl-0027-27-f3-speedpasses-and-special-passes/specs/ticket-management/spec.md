# Spec Delta

## ADDED Requirements

### Requirement: Ticket deletion removes associated passes
When a future individual ticket is validly deleted with `TICKET DELETE`, all its SpeedPasses and its Special Pass SHALL be removed with it. When a future group ticket is validly deleted with `GROUPTICKET DELETE`, the passes of its composed tickets, the composed tickets and the group ticket SHALL be removed together. If the deletion is not allowed, nothing SHALL be removed.

#### Scenario: Individual ticket with passes
- **WHEN** future ticket `T001` has passes `SP01:1`, `SP02:1`…`SP02:10` and `PE01` and `TICKET DELETE;T001` is processed
- **THEN** `T001` and all those passes no longer exist

#### Scenario: Group ticket with passes
- **WHEN** future group ticket `TG01` has a SpeedPass on `TG01:12345678Z` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** the pass, the composed tickets and `TG01` no longer exist

#### Scenario: Deletion refused keeps passes
- **WHEN** `T001` is dated today and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` and its passes still exist
