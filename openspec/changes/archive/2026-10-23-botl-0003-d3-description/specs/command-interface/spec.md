# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the syntax of every command recognised by the application across Deliveries 1, 2 and 3, including `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP EXTENDED`, `HELP` and `EXIT`.

#### Scenario: Show help
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`

#### Scenario: Delivery 2 commands in help
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET` and `ATTRACTION` command

#### Scenario: Delivery 3 commands in help
- **WHEN** `HELP` is processed
- **THEN** the output also lists the syntax of the typed `ATTRACTION CREATE`, `ATTRACTION ENTER ... ;SPEED`, every `SPEEDPASS`, `SPECIALPASS` and `INVOICE` command, and `HELP EXTENDED`

## ADDED Requirements

### Requirement: Extended help
The `HELP EXTENDED` command SHALL display, for every command shown by `HELP`, its syntax together with a brief description of its purpose.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command's syntax is shown with a one-line explanation of what it does
