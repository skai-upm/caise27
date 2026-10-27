# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 commands coexist
The commands of Deliveries 1, 2 and 3 SHALL work together in one session from standard input or a command file, with and without `--fly`, and HELP SHALL show at least the Delivery 3 syntax list (`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED`, `EXIT`) together with the Delivery 1 and 2 syntax.

#### Scenario: Complete Delivery 3 session
- **WHEN** `input_fase3.txt` creates users and a group, buys individual and group tickets and passes, creates typed attractions, enters with SPEED (individual and group) and at a SPECIAL attraction, refunds a pass, prints invoices and ends with `EXIT`
- **THEN** every operation produces the expected result, consumed passes remain stored, and running the same file with `--fly` produces the same output and leaves no data

#### Scenario: Rejected operations in the session
- **WHEN** the session also tries a group ticket at an INDIVIDUAL attraction, SPEED at a SPECIAL attraction, a group SPEED entry where one member lacks a SpeedPass, and a refund of a consumed pass
- **THEN** each is rejected with an error, nobody enters and no pass changes, and the session continues

#### Scenario: Help shows all deliveries
- **WHEN** the user enters `HELP`
- **THEN** the Delivery 1, 2 and 3 command syntaxes are all shown
