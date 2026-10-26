# Spec Delta

## ADDED Requirements

### Requirement: Complete help
Every command accepted by the application, from all deliveries, SHALL be listed with its syntax by `HELP` and with its description by `HELP EXTENDED`, and every command they list SHALL be accepted by the application.

#### Scenario: Delivery 3 commands in help
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both include `INVOICE CREATE`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE` and the typed `ATTRACTION CREATE` and `SPEED` entry syntax, together with every E1 and E2 command
