# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice contents
A user invoice SHALL list, with their identifier, date and price, the individual tickets bought directly for that user, and SHALL NOT list or charge the individual tickets that belong to a group ticket; it SHALL also list, with identifier and price, the personal passes attached to any individual ticket of that user (including constituent tickets of group purchases); its total SHALL be the sum of the listed prices.

#### Scenario: Direct ticket charged once, group constituent not charged
- **GIVEN** Ana bought `T001` at `40.00` and is a member included in group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` with `40.00`, does not list `TG01` or its constituent ticket, and the total is `40.00`

#### Scenario: Personal passes on a constituent ticket
- **GIVEN** Ana is included in `TG01` and bought `SPEEDPASS CREATE;SP01;TG01-12345678Z;1` (`8.00`)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists pass `SP01-1` with `8.00` in a passes section and does not charge the constituent ticket; the group invoice of the group does not include the pass
