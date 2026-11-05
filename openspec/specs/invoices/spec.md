# invoices Specification

## Purpose
Defines on-demand invoice generation for users and groups, built step by step with the Builder pattern from the economic information stored in the park, including only the relevant sections with content and never counting a ticket that is already part of a group ticket twice.

## Requirements

### Requirement: Invoices are constructed with the Builder pattern
The system SHALL build every invoice through a common builder abstraction whose concrete builders (one for users, one for groups) construct the invoice in clearly differentiated steps (recipient identification, purchases, personal passes) directed by a single director, and SHALL NOT build invoices through a constructor with many optional parameters or through `null` values used only to complete a common signature. A section SHALL be added to the invoice only when it is relevant to the recipient type and has at least one item; sections with no information SHALL be omitted. Invoices SHALL be generated on demand from the current system state and SHALL NOT be persisted.

#### Scenario: Only sections with content are included
- **GIVEN** a user without any purchase
- **WHEN** `INVOICE CREATE;USER;<email>` is executed for that user
- **THEN** the response shows the user identification and a total of `0.00` EUR
- **AND** it shows no empty purchase section

#### Scenario: Invoice generation does not change stored data
- **GIVEN** a park with users, groups and tickets stored in its data files
- **WHEN** a user invoice and a group invoice are generated
- **THEN** the data files are unchanged

### Requirement: User invoice
The `INVOICE CREATE;USER;<email>` command SHALL generate an invoice for the active (registered) user with that email. The invoice SHALL contain the user's identifying information (name, surnames, DNI and email), a section with the individual tickets purchased directly for that user, each with its identifier, date and price, the personal passes associated with the user's individual tickets when they exist, and the total amount of the items shown. Individual tickets that are components of a group ticket SHALL NOT be included as individual purchases nor added to the total. When no user has that email, the command SHALL be rejected with an `ERROR` response.

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

### Requirement: Group invoice
The `INVOICE CREATE;GROUP;<groupIdentifier>` command SHALL generate an invoice for the existing group with that identifier. The invoice SHALL contain the group identifier and name, a section with the group tickets purchased for that group, each with its identifier, date, number of tickets, applied discount and total price, the composition of each group ticket (holder and component ticket) as descriptive detail, and the total amount of the group tickets shown. The component individual tickets SHALL NOT be added separately to the total, and passes associated with members' individual tickets SHALL NOT be added to the group invoice. When no group has that identifier, the command SHALL be rejected with an `ERROR` response.

#### Scenario: Group invoice counts each group ticket once
- **GIVEN** group `G01` "Familia" with 4 members and a group ticket `GT1` priced 144.00 EUR (4 component tickets at 36.00 EUR each)
- **WHEN** `INVOICE CREATE;GROUP;G01` is executed
- **THEN** the response shows `G01` and `Familia`
- **AND** it lists `GT1` with its date, 4 tickets, a 10% discount and the price `144.00` EUR
- **AND** it describes the 4 component tickets with their holders
- **AND** the total is `144.00` EUR, not `288.00` EUR

#### Scenario: Members' own tickets are not on the group invoice
- **GIVEN** a member of group `G01` also holds a stand-alone individual ticket `T9`
- **WHEN** `INVOICE CREATE;GROUP;G01` is executed
- **THEN** `T9` is not listed and its price is not part of the total

#### Scenario: Group without purchases
- **GIVEN** group `G02` has no group tickets
- **WHEN** `INVOICE CREATE;GROUP;G02` is executed
- **THEN** the response shows the group identification and a total of `0.00` EUR with no group ticket section

#### Scenario: Unknown group is rejected
- **WHEN** `INVOICE CREATE;GROUP;G99` is executed and no group has that identifier
- **THEN** the response starts with `ERROR` and no invoice is shown

### Requirement: Invoice command syntax
The `INVOICE CREATE` command SHALL take exactly three elements, the recipient type `USER` or `GROUP` followed by the recipient key; any other recipient type or number of elements SHALL be rejected with an `ERROR` response describing the expected syntax, without changing any data.

#### Scenario: Unknown recipient type
- **WHEN** `INVOICE CREATE;COMPANY;X1` is executed
- **THEN** the response starts with `ERROR` and names the accepted forms `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`

#### Scenario: Missing recipient key
- **WHEN** `INVOICE CREATE;USER` is executed
- **THEN** the response starts with `ERROR`
