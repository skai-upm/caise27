# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the syntax of every command of Deliveries 1, 2 and 3, including at least `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP EXTENDED`, and all Delivery 1 and 2 syntaxes.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** the syntax of every Delivery 1, 2 and 3 command is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every command's syntax with a brief description of its purpose. The help generator SHALL receive the behaviour that builds its output through dependency injection, so that new help formats can be added without changing command reading and execution.

#### Scenario: Extended help output
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax is printed together with a description of its purpose
