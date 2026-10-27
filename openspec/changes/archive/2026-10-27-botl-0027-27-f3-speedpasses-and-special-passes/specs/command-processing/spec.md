# Spec Delta

## ADDED Requirements

### Requirement: HELP lists pass commands
HELP and HELP EXTENDED SHALL also include `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>`.

#### Scenario: Help shows pass commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains the four pass command syntaxes
