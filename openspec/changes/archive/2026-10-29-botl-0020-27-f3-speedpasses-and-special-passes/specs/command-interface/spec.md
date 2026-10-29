# Spec Delta

## ADDED Requirements

### Requirement: HELP lists pass commands
The HELP and HELP EXTENDED outputs SHALL include `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>`.

#### Scenario: HELP shows pass commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains the four pass command syntaxes
