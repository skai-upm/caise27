## MODIFIED Requirements

### Requirement: User invoice
The `INVOICE CREATE;USER;<email>` command SHALL generate an invoice for the active (registered) user with that email. The invoice SHALL contain the user's identifying information (name, surnames, DNI and email), a section with the individual tickets purchased directly for that user, each with its identifier, date and price, a section with the SpeedPasses and a section with the Special Passes associated with any individual ticket held by the user (including tickets that are components of a group ticket, because passes are personal purchases), each pass with its identifier, ticket and price, and the total amount of the items shown; each pass section SHALL be omitted when there are no such passes. Individual tickets that are components of a group ticket SHALL NOT be included as individual purchases nor added to the total. When no user has that email, the command SHALL be rejected with an `ERROR` response.

#### Scenario: User invoice lists direct individual tickets with their total
- **GIVEN** user `ana@correo.com` holds individual tickets `T1` and `T2` bought at 40.00 EUR each
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is executed
- **THEN** the response identifies Ana by name, surnames, DNI and email
- **AND** it lists `T1` and `T2`, each with its date and the price `40.00` EUR
- **AND** the total is `80.00` EUR

#### Scenario: Group-ticket components are not counted on the user invoice
- **GIVEN** user `ana@correo.com` holds individual ticket `T1` at 40.00 EUR and is a member of group `G01`, for which group ticket `GT1` was bought, containing the component ticket `GT1:<Ana's DNI>`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is executed
- **THEN** the response lists `T1` and does not list `GT1:<Ana's DNI>`
- **AND** the total is `40.00` EUR

#### Scenario: Unknown user is rejected
- **WHEN** `INVOICE CREATE;USER;nobody@correo.com` is executed and no user has that email
- **THEN** the response starts with `ERROR` and no invoice is shown

#### Scenario: Passes appear on the user invoice
- **GIVEN** user `ana@correo.com` holds individual ticket `T1` at 40.00 EUR with SpeedPass `SP01:1` (8.00 EUR), and her component ticket `GT1:<Ana's DNI>` at 36.00 EUR has Special Pass `PE01` (36.00 EUR)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is executed
- **THEN** the response lists `T1`, a SpeedPasses section with `SP01:1` at `8.00` EUR and a Special Passes section with `PE01` at `36.00` EUR
- **AND** it does not list `GT1:<Ana's DNI>` as a ticket purchase
- **AND** the total is `84.00` EUR
