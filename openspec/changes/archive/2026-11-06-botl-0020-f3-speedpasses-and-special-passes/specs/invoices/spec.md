# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
The system SHALL, for `INVOICE CREATE;USER;<email>` naming an active registered user, display an invoice with the user's identifying information (email and DNI), a section listing each individual ticket purchased directly for that user with its identifier, date and price, a section with the SpeedPasses and a section with the Special Passes associated with any of the user's individual tickets (including tickets that compose a group ticket), each with its price, and the total of the listed items. Individual tickets that are part of a group ticket SHALL NOT be listed or counted. A user who does not exist SHALL produce an error message.

#### Scenario: User with direct and group tickets
- **GIVEN** `ana@correo.com` (DNI `12345678Z`) has direct tickets `T001` (40.00) and `T002` (45.50) and holds `TG01:12345678Z` in group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows `ana@correo.com` and `12345678Z`
- **AND** lists `T001` and `T002` with their prices
- **AND** does not list `TG01:12345678Z`
- **AND** shows the total 85.50

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no such user exists
- **THEN** an error message is displayed

#### Scenario: Passes on the user invoice
- **GIVEN** `ana@correo.com` has direct ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00), and composed ticket `TG01:12345678Z` (40.00) with Special Pass `PE01` (40.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, `SP01:1` and `PE01` but not `TG01:12345678Z`
- **AND** shows the total 88.00
- **AND** `INVOICE CREATE;GROUP` for the group of `TG01` does not include `PE01`
