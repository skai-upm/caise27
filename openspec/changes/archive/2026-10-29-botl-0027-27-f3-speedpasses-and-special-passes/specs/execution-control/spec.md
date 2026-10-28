# Spec Delta

## ADDED Requirements

### Requirement: HELP lists pass commands
`HELP` and `HELP EXTENDED` SHALL also list `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>`.

#### Scenario: Help shows passes
- **WHEN** `HELP` is processed
- **THEN** the four pass command syntaxes are shown
