# Spec Delta

## ADDED Requirements

### Requirement: Passes on user invoices
A user invoice SHALL include, in separate SpeedPass and Special Pass sections with their prices counted in the total, the passes associated with the user's individual tickets, including tickets that belong to a group ticket. Group invoices SHALL NOT add members' passes.

#### Scenario: Passes of a composed ticket
- **WHEN** Ana's composed ticket `TG01:12345678Z` has SpeedPass `SP01:1` (8.00) and Ana has no direct ticket, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows a SpeedPasses section with `SP01:1` at `8.00`, no individual tickets section, and a total of `8.00`

#### Scenario: Group invoice unaffected
- **WHEN** the group invoice of `G01` is generated
- **THEN** its total is the sum of its group tickets only
