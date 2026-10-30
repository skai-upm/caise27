# Spec Delta

## MODIFIED Requirements

### Requirement: Command syntax
Commands SHALL consist of elements separated by `;` and be interpreted using basic String operations including split, without external libraries. The recognised commands are those of Deliveries 1 and 2 plus `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, `HELP EXTENDED`, the typed `ATTRACTION CREATE` and the `SPEED` variant of `ATTRACTION ENTER`.

#### Scenario: Unknown command
- **WHEN** a line such as `FOO BAR` or `USER LIST` is processed
- **THEN** an `ERROR:` message is printed and the application keeps running

### Requirement: HELP command
The `HELP` command SHALL display the syntax of every command of the three deliveries, including at least `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** every syntax of Deliveries 1, 2 and 3 is printed

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every command syntax together with a brief description of its purpose. The help output SHALL be produced by a generator whose formatting behaviour is injected, independent of command reading and execution.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** each command syntax is printed followed by a description of its purpose
