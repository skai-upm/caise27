## ADDED Requirements

### Requirement: Delivery 3 HELP content
`HELP` SHALL display the commands of all three deliveries, each listed syntax form on its own line, including at least, in this order, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`, while the Delivery 1 and 2 syntax lines continue to appear. `HELP EXTENDED` SHALL describe the purpose of every command.

#### Scenario: Delivery 3 syntax lines in order
- **WHEN** `HELP` is processed
- **THEN** each listed Delivery 3 syntax appears on a line of its own, in the listed order, and the Delivery 2 list still appears in its order

#### Scenario: Extended help covers all commands
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax line shown by `HELP` is accompanied by a purpose description
