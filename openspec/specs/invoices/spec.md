# invoices Specification

## Purpose

Produces on-demand invoices for a user or a group from the stored economic data, listing each purchased item with its price and the total, without double counting.

## Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print an invoice for an active user. It shows the user's name, DNI, and email. It lists, each with its price: standalone individual tickets bought for the user; SpeedPasses and Special Passes of the user's individual tickets, including those inside group tickets; and the total of the listed items. Individual tickets inside group tickets SHALL NOT be listed or counted. Sections without items SHALL be omitted. An unknown user SHALL be rejected with an error.

#### Scenario: Ana's invoice
- **WHEN** Ana has ticket `T001` (40.00), ten SpeedPasses of 8.00 on `T001`, Special Pass `PE01` (40.00), one SpeedPass of 7.20 on `TG01:12345678Z`, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, the eleven SpeedPasses, and `PE01`, does not list `TG01:12345678Z` as a ticket, and shows total `167.20`

#### Scenario: Empty sections omitted
- **WHEN** a user with only one standalone ticket is invoiced
- **THEN** the invoice has a tickets section and no SpeedPass or Special Pass section

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupId>` SHALL print an invoice for an existing group. It shows the group identifier and name, each group ticket bought by the group with its date and total price, the composition of each group ticket as non-counted detail, and the total of the group tickets. Passes of the members SHALL NOT be counted. An unknown group SHALL be rejected with an error.

#### Scenario: G01's invoice
- **WHEN** group `G01` bought `TG01` (3 members, 108.00) and members have SpeedPasses, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` at `108.00` with its three individual tickets as detail, and shows total `108.00`

#### Scenario: Invalid invoice request
- **WHEN** `INVOICE CREATE;COMPANY;X` or `INVOICE CREATE;GROUP;G99` (unknown) is processed
- **THEN** an error is printed
