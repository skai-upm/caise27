# ticketing Specification

## Purpose

Sells individual and group tickets for a given date, prices them with the current ticket price and group discounts, enforces deletion rules based on the system date, and keeps tickets as persistent history.

## Requirements

### Requirement: Ticket identity
Every ticket SHALL have an identifier unique among all tickets (individual, group, and those generated inside group tickets), a date of use, and a price. Identifiers SHALL consist only of letters, digits, `-` and `_`. Individual and group tickets SHALL be usable through a common ticket abstraction, for example when looking a ticket up by identifier.

#### Scenario: Duplicate identifier
- **WHEN** ticket T001 exists and another individual or group ticket is created with identifier T001
- **THEN** an error is shown and no ticket is created

### Requirement: Buy individual ticket
`TICKET CREATE;Id;Email;yyyy-MM-dd` SHALL create a ticket for the registered user with that email, for that date, at the current ticket price, holding a reference to the user, and SHALL reply `OK TICKET CREATED -> <price>` with the price in two decimals. Unknown users, invalid dates and dates before the system date SHALL be rejected.

#### Scenario: Ticket bought
- **WHEN** Ana exists, the price is 40.00 and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed with system date 2026-10-01
- **THEN** the reply is `OK TICKET CREATED -> 40.00` and T001 belongs to Ana

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is executed
- **THEN** an error is shown and no ticket is created

#### Scenario: Date before system date
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T003;ana@correo.com;2026-09-30` is executed
- **THEN** an error is shown and no ticket is created

### Requirement: Delete individual ticket
`TICKET DELETE;Id` SHALL delete an individual ticket only when its date is strictly after the system date. Tickets dated on or before the system date, unknown identifiers, and tickets generated inside a group ticket SHALL be rejected.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** T001 is deleted

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** an error is shown and T001 remains

### Requirement: Buy group ticket
`GROUPTICKET CREATE;Id;GroupId;yyyy-MM-dd` SHALL generate one individual ticket at the current price for each user in the group at that moment, compose them into a group ticket that references the group, and reply `OK GROUP TICKET CREATED -> <price>`. The group ticket price SHALL be the sum of the individual prices minus a discount by member count: fewer than 3 → 0%, 3–5 → 10%, 6–10 → 20%, more than 10 → 50%. Unknown or empty groups, invalid dates and dates before the system date SHALL be rejected.

#### Scenario: Discount brackets
- **WHEN** the price is 40.00 and a group ticket is bought for groups of 2, 3, 5, 6, 10 and 11 members
- **THEN** the prices are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00 respectively

#### Scenario: Composition frozen at purchase
- **WHEN** a group ticket is bought for G01 with members Ana and Pedro, and the group later changes
- **THEN** the group ticket still contains exactly the individual tickets of Ana and Pedro

### Requirement: Delete group ticket
`GROUPTICKET DELETE;Id` SHALL delete a group ticket and all its generated individual tickets only when its date is strictly after the system date. Otherwise, or for unknown identifiers, it SHALL be rejected.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is 2026-10-01 and `GROUPTICKET DELETE;TG01` is executed for a group ticket dated 2026-10-15
- **THEN** TG01 and its individual tickets no longer exist

### Requirement: Ticket history
Tickets dated before the system date SHALL be retained as historical records, including tickets of users or groups deleted later.

#### Scenario: Past ticket kept
- **WHEN** the system date moves past a ticket's date
- **THEN** the ticket still exists and is still stored

### Requirement: Ticket persistence
Individual tickets, group tickets and their generated individual tickets SHALL be stored in text files next to the users file and restored at startup, linked to the restored users and groups.

#### Scenario: Tickets restored
- **WHEN** T001 for Ana and TG01 for G01 were saved and the application restarts
- **THEN** both tickets exist with the same dates, prices, user and group, and the users' deletion locks still apply
