## ADDED Requirements

### Requirement: Passes on invoices
A user invoice SHALL include, in separate sections, the SpeedPasses and the Special Passes associated with any of the user's individual tickets (including composition tickets of group tickets), with their prices added to the total. A group invoice MUST NOT add members' passes to its total.

#### Scenario: Passes of a composition ticket on the user invoice
- **GIVEN** Ana's composition ticket `TG01:12345678Z` (40.00, already in the group ticket) has SpeedPass `SP01:1` (8.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `SP01:1` with 8.00 in a SpeedPasses section, not the composition ticket, and the total includes 8.00

#### Scenario: Group invoice excludes passes
- **GIVEN** the same situation
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the total equals the sum of the group tickets' prices only
