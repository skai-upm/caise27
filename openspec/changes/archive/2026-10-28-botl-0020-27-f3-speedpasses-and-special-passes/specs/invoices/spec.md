## MODIFIED Requirements

### Requirement: User invoice
The system SHALL accept `INVOICE CREATE;USER;<email>` for an active user and display an invoice with the user's identifying information, a section with the individual tickets purchased directly for that user and the price of each, sections with the SpeedPasses and Special Passes associated with any of the user's individual tickets (including tickets inside group tickets) and the price of each, and the total of the items shown. Individual tickets that are part of a group ticket composition SHALL NOT be included as individual purchases. An unknown or deleted user SHALL be rejected with an error message.

#### Scenario: User with direct tickets
- **WHEN** user `ana@correo.com` has individual tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice identifies the user, lists `T001` with 40.00 and `T002` with 45.50, and shows a total of 85.50

#### Scenario: Group ticket items not counted again
- **WHEN** `ana@correo.com` only appears in the composition of group ticket `TG01` and her user invoice is requested
- **THEN** no ticket item is listed and the total is 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error message is displayed

#### Scenario: Passes on a group-composed ticket
- **WHEN** `ana@correo.com` only appears in group ticket `TG01` (32.00 per member) and bought a SpeedPass (6.40) and a Special Pass (32.00) for `TG01:12345678Z`
- **THEN** her invoice lists both passes, no ticket item, and a total of 38.40, while the group invoice of the group does not include those passes
