# Spec Delta

## MODIFIED Requirements

### Requirement: Restore passes at startup
On startup the system SHALL load the passes after the tickets and link each to its individual or constituent ticket, keeping price and consumed status; a missing file SHALL mean no passes and records whose ticket no longer exists SHALL be skipped.

#### Scenario: Restart
- **GIVEN** a previous run bought `S1-1` and `SP1` for `T001`
- **WHEN** the application restarts
- **THEN** both passes exist with their prices, `SPECIALPASS CREATE;SP2;T001` is rejected and `SPEEDPASS CREATE;S1;T001;1` is rejected as a duplicate identifier

#### Scenario: Consumed pass restored
- **GIVEN** `S1-1` was consumed by a SPEED entry
- **WHEN** the application restarts
- **THEN** `S1-1` keeps its identifier, price 8.00, ticket `T001` and consumed status
