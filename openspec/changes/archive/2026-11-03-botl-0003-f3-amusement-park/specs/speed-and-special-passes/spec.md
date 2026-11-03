# Spec Delta

## Purpose

Lets users buy SpeedPasses and Special Passes for individual tickets, keeps them as purchase records, and refunds unused future ones.

## ADDED Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among all passes, a stored price, its associated individual ticket and a consumed indicator; consumed passes SHALL remain stored as purchase records.

#### Scenario: Duplicate pass identifier
- **WHEN** a pass purchase would create an identifier already used by another pass
- **THEN** the purchase is rejected and nothing is created

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL, for an existing individual ticket (standalone or composed) dated on or after the current system date and quantity 1 or 10, create that many independent SpeedPasses identified `<purchaseIdentifier>:<number>`, each costing 20 % of the ticket's stored price, and display the total; any identifier conflict SHALL reject the whole purchase.

#### Scenario: Ten-pack
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` runs for a 40.00 ticket
- **THEN** passes `SP02:1` to `SP02:10` are created at 8.00 each and the total 80.00 is displayed

#### Scenario: Single pass identifier
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` runs
- **THEN** one pass `SP01:1` is created

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is not 1 or 10, the ticket is a group ticket or unknown, or the ticket is dated before today
- **THEN** the purchase is rejected

#### Scenario: Discounted composed ticket
- **WHEN** a SpeedPass is bought for a composed ticket priced 36.00
- **THEN** the pass costs 7.20

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL, for an existing individual ticket dated on or after the current system date that has no Special Pass, create one costing the ticket's stored price and display it; a consumed Special Pass SHALL still prevent a second purchase.

#### Scenario: Buy Special Pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` runs for a 40.00 ticket
- **THEN** the pass is created and 40.00 is displayed

#### Scenario: Second Special Pass
- **WHEN** a second Special Pass is requested for a ticket that already has one, consumed or not
- **THEN** the purchase is rejected

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete one pass of that kind only if it is unconsumed and its ticket is dated strictly after the current system date.

#### Scenario: Refund one pass of a pack
- **WHEN** `SPEEDPASS DELETE;SP02:3` runs for an unused pass of a future ticket
- **THEN** only `SP02:3` is removed

#### Scenario: Refund rejected
- **WHEN** a refund targets a consumed pass or a pass whose ticket is dated today
- **THEN** the refund is rejected and the pass remains

### Requirement: Cascading ticket deletion
A valid `TICKET DELETE` SHALL also delete all passes of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete the passes of its composed tickets, the composed tickets and the group ticket, all or nothing.

#### Scenario: Ticket deletion removes passes
- **WHEN** a future ticket with SpeedPasses from two purchases and a Special Pass is deleted
- **THEN** the ticket and all its passes are removed
