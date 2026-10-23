# Spec Delta

## Purpose

Generates on-demand invoices for a user or a group from the stored park state, showing each purchase that belongs to the recipient with its price and the total.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print an invoice for an existing active user containing the user's identifying data, the individual tickets bought directly for the user (not member tickets of group tickets), the SpeedPasses and Special Passes of all the user's individual tickets (including member tickets), the price of each item and the total of the items shown. Consumed passes are included.

#### Scenario: Personal purchases only
- **WHEN** Ana has T001 (40.00), ten SpeedPasses for T001 (8.00 each), Special Pass PE01 (40.00), a member ticket `TG01:12345678Z` and SpeedPass SP02:1 (7.20) for it
- **THEN** her invoice lists T001, the eleven SpeedPasses and PE01, does not list `TG01:12345678Z` as a purchase, and shows a total of 167.20 EUR

#### Scenario: Unknown user
- **WHEN** no active user has the email
- **THEN** an error is printed and no invoice is shown

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupId>` SHALL print an invoice for an existing group containing the group identifier and name, each group ticket bought by the group with its date, composition and total price, and the total of those group tickets. Member tickets SHALL NOT be added separately, and members' passes SHALL NOT be included.

#### Scenario: Group tickets
- **WHEN** G01 bought TG01 (3 members, 108.00 EUR) and members bought SpeedPasses
- **THEN** the invoice lists TG01 with its three member tickets and shows a total of 108.00 EUR

#### Scenario: Unknown group
- **WHEN** the group does not exist
- **THEN** an error is printed

### Requirement: Only applicable sections
An invoice SHALL contain only the sections that apply to its recipient type and have content; empty sections SHALL be omitted, and an invoice with no purchases SHALL show a total of 0.00 EUR.

#### Scenario: User without passes
- **WHEN** a user only has individual tickets
- **THEN** the invoice has a tickets section and no SpeedPass or Special Pass sections

#### Scenario: Invalid invoice type
- **WHEN** `INVOICE CREATE;SHOP;X` is processed
- **THEN** an error naming the valid types `USER` and `GROUP` is printed
