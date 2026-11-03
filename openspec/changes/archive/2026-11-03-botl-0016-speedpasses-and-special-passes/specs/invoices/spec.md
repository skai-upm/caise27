# Spec Delta

## ADDED Requirements

### Requirement: Personal passes on user invoices
A user invoice SHALL include a "Personal passes" section listing every pass associated with the user's individual tickets, including passes on constituent tickets of group tickets, with their prices; the constituent tickets themselves SHALL still not be charged to the user, and group invoices SHALL NOT include members' passes.

#### Scenario: Pass on a constituent ticket
- **GIVEN** Ana belongs to `TG01` and bought `SPECIALPASS CREATE;SP1;TG01-1` for 36.00
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` and `INVOICE CREATE;GROUP;G01` are processed
- **THEN** Ana's invoice lists `SP1` 36.00 and no `TG01-1` ticket, and the group invoice total does not include 36.00
