# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print an invoice for the active user with that email containing the user's identifying information (name, surnames, DNI, email), a section with every individual ticket purchased directly for the user with its identifier, date and price, a section with the SpeedPasses and a section with the Special Passes associated with any of the user's individual tickets (including constituents of group tickets), each with its price, and the total of the items shown. Individual tickets that belong to a group ticket's composition MUST NOT appear as individual purchases. An email of no active user MUST be rejected with an error.

#### Scenario: Direct tickets only
- **GIVEN** Ana bought individual tickets `T001` (40.00) and `T002` (45.50) and also has constituent ticket `TG01:12345678Z` of a group ticket
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows Ana's identification, `T001` and `T002` with their prices, does not list `TG01:12345678Z`, and the total is `85.50`

#### Scenario: Unknown or deleted user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error reporting that no user has that email is printed

#### Scenario: Passes on own and group tickets
- **GIVEN** Ana has ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00), and constituent `TG01:12345678Z` (36.00) with Special Pass `PE01` (36.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, a SpeedPasses section with `SP01:1`, a Special passes section with `PE01`, does not list `TG01:12345678Z` as a ticket, and the total is `84.00`
