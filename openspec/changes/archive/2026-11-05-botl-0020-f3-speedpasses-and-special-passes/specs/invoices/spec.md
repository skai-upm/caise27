# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<email>` SHALL show an invoice for an existing active user. It SHALL contain the user's identifying information, a section listing each individual ticket bought directly for that user (identifier, date and price), a section listing the SpeedPasses and Special Passes associated with that user's individual tickets (including tickets that make up a group ticket) with their prices, and a total equal to the sum of the listed items. Sections with no items SHALL be omitted. Individual tickets that make up a group ticket SHALL NOT be listed or counted on the user invoice. An unknown user SHALL be rejected with an error.

#### Scenario: Direct tickets invoiced
- **GIVEN** `ana@correo.com` with direct tickets `T001` (40.00) and `T002` (45.50), and a component ticket `TG01:12345678Z` (36.00) of a group ticket
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows the user's e-mail and DNI, lists `T001` and `T002` with their prices, does not list `TG01:12345678Z`, and shows total 85.50

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nobody@correo.com` is processed
- **THEN** an error is shown and no invoice is displayed

#### Scenario: Personal passes invoiced
- **GIVEN** `ana@correo.com` with direct ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00), and group component `TG01:12345678Z` (36.00) with Special Pass `PE02` (36.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, `SP01:1` and `PE02`, does not list `TG01:12345678Z`, and shows total 84.00
