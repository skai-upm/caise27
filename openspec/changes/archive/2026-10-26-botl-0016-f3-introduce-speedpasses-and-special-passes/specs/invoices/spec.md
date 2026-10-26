# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL generate an invoice for the registered user with that email, listing the individual tickets bought directly for that user with their identifier, date and price, the personal passes attached to any of the user's individual tickets (including tickets inside group tickets) with their identifier and price, and the total. Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases.

#### Scenario: User with direct and group tickets
- **WHEN** Ana bought T001 (40.00) directly and is a member of a group that bought TG01, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001 with 40.00, does not list any ticket of TG01, and the total is 40.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is reported

#### Scenario: Passes on an internal ticket
- **WHEN** Ana's ticket TG01-1 inside group ticket TG01 has a SpeedPass of 8.00 and Ana bought T001 (40.00)
- **THEN** her invoice lists T001 and the SpeedPass, not TG01-1, and the total is 48.00
