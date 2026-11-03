# Spec Delta

## MODIFIED Requirements

### Requirement: Entry rejections leave occupancy unchanged
Entry SHALL be rejected without changing occupancy or passes when the attraction or ticket does not exist, the ticket date differs from the current system date, the attraction is full, a user who would enter is already inside that attraction, the attraction type does not accept the ticket kind, or a required SpeedPass or Special Pass is not available.

#### Scenario: Ticket for another date
- **WHEN** ticket `T001` is dated tomorrow and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error is reported and `A01` occupancy is unchanged

#### Scenario: Full attraction
- **WHEN** `A01` is at capacity and a valid ticket for today is used to enter
- **THEN** an error is reported and occupancy is unchanged

#### Scenario: Ticket kind not accepted
- **WHEN** a valid individual ticket is used at a GROUP attraction
- **THEN** an error is reported and occupancy is unchanged
