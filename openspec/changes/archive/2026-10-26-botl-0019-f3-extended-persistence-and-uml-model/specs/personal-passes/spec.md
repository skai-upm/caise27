# Spec Delta

## ADDED Requirements

### Requirement: Passes follow their ticket
Personal passes SHALL be deleted together with the individual ticket they belong to, both when an individual ticket is refunded and when a group ticket is refunded. If storing the result fails, the deletion SHALL be undone so no ticket or pass is partially deleted, and the response is `ERROR STORAGE FAILURE`.

#### Scenario: Storage failure during refund
- **WHEN** the pass file cannot be written while `TICKET DELETE;T001` is processed for a ticket with passes
- **THEN** the response is `ERROR STORAGE FAILURE` and T001 and its passes still exist

### Requirement: Passes and their state survive restarts
After a restart every pass SHALL be attached to the same individual ticket with the same identifier, kind, price and consumed status it had before.

#### Scenario: Consumed status and price preserved
- **WHEN** a SpeedPass bought at 8.00 is consumed and the application restarts
- **THEN** the pass exists on its ticket with price 8.00 and consumed status
