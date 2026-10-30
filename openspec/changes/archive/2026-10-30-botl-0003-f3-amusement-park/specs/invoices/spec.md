# Spec Delta

## Purpose

Produces on-demand invoices for users and groups from the system's economic information, including only the sections relevant to each recipient.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, print an invoice with the user's identifying data and, only when non-empty, sections for stand-alone individual tickets bought for the user, SpeedPasses and Special Passes of any of the user's individual tickets (including those inside group tickets), each with its price, plus the total of the items shown. Composition tickets SHALL NOT be listed as individual purchases.

#### Scenario: User with ticket and passes
- **WHEN** Ana has ticket T001 (40.00), SpeedPass `SP01:1` (8.00) and Special Pass PE01 (40.00)
- **THEN** the invoice lists the three items and a total of 88.00

#### Scenario: Member of a group ticket
- **WHEN** Pedro's only purchases are a SpeedPass on `TG01:87654321X`
- **THEN** his invoice shows that SpeedPass, no ticket section and a total equal to the SpeedPass price

#### Scenario: Unknown user
- **WHEN** the email does not belong to an active user
- **THEN** an error is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, print the group identifier and name, each group ticket bought by the group with its composition and total price, and the sum of the group ticket totals. Composition tickets and members' passes SHALL NOT be added to the total.

#### Scenario: Group invoice total
- **WHEN** G01 bought TG01 (2 members, 80.00) and a member also bought a SpeedPass
- **THEN** the invoice shows TG01 with both member tickets and a total of 80.00

#### Scenario: Unknown group
- **WHEN** the group does not exist
- **THEN** an error is printed

### Requirement: Omit empty sections
Invoice sections without items SHALL be omitted rather than printed empty.

#### Scenario: User without purchases
- **WHEN** a user with no tickets or passes requests an invoice
- **THEN** only the user data and a total of 0.00 are printed
