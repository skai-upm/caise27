# invoices Specification

## Purpose

Produces on-demand invoices for users and groups from the stored park state, showing only the items that apply to the recipient and their correct total.

## Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, print an invoice with the user's identifying data, the stand-alone individual tickets bought for the user, the SpeedPasses and Special Passes of all the user's individual tickets (including group-ticket components), the price of each item and the total of the items shown. Group-ticket components SHALL NOT be listed as ticket purchases. Sections without items SHALL be omitted.

#### Scenario: Ticket and passes
- **WHEN** Ana has T001 (40.00) with ten SpeedPasses (8.00 each) and Special Pass PE01 (40.00), plus a SpeedPass (7.20) on her component of TG01, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001, eleven SpeedPasses and PE01, does not list TG01, and shows a total of 167.20

#### Scenario: Empty sections omitted
- **WHEN** an invoice is created for a user who has no passes
- **THEN** no SpeedPass or Special Pass section appears

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nobody@correo.com` is processed
- **THEN** an error is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, print an invoice with the group identifier and name, each group ticket bought by the group with its composition and total price, and the total of those group tickets. Members' passes SHALL NOT be included, and component tickets SHALL NOT be added again to the total.

#### Scenario: Group with one group ticket
- **WHEN** G01 bought TG01 for three members at 40.00 (10 % discount) and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows TG01 with its three members and a total of 108.00

#### Scenario: Unknown group or recipient type
- **WHEN** `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error is printed
