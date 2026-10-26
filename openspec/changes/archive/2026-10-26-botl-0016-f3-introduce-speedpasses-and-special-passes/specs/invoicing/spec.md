# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<correo>` SHALL produce an invoice for the registered user with that email listing the individual tickets bought directly for that user (identifier, date and price). Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases. The personal passes attached to any of the user's individual tickets, including internal tickets of group tickets, SHALL appear as their own items with their prices in a separate section. An unknown email SHALL be rejected with an `ERROR` line.

#### Scenario: Direct tickets listed
- **WHEN** Ana bought `T001` (40.00) and `T002` (60.00) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` and `T002` with their prices and a total of 100.00

#### Scenario: Group ticket members not double counted
- **WHEN** Ana is covered by group ticket `TG01` through internal ticket `TG01-1` and has no direct tickets or passes
- **THEN** her invoice lists no individual tickets and its total is 0.00

#### Scenario: Passes on internal tickets
- **WHEN** Ana bought a SpeedPass (8.00) for her internal ticket `TG01-1`
- **THEN** her invoice lists that SpeedPass in a passes section and totals 8.00, while `TG01-1` itself is not listed

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an `ERROR` line is printed
