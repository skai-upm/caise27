# Spec Delta

## ADDED Requirements

### Requirement: HELP shows the Delivery 3 minimum set
The HELP output SHALL contain the commands of the three deliveries, including at least `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`; HELP EXTENDED SHALL describe each of them.

#### Scenario: Delivery 3 syntax present
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every syntax of the Delivery 3 minimum set appears in both, and in HELP EXTENDED each is followed by a description
