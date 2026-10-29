# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes attached to individual tickets: purchase, pricing, consumption state, refunds and deletion together with their ticket.

## ADDED Requirements

### Requirement: Pass data
Every pass SHALL have an identifier unique among existing passes (shared by SpeedPasses and Special Passes, independent from tickets), a price, its associated individual ticket and a consumed flag. Consumed passes MUST remain stored while their ticket exists.

#### Scenario: Consumed pass kept
- **WHEN** a pass is consumed on entry
- **THEN** it still exists, marked as consumed

### Requirement: Pass prices
A SpeedPass SHALL cost 20 % of the stored price of its individual ticket and a Special Pass SHALL cost the stored price of its individual ticket (the discounted price for composition tickets). SYSTEM PRICE changes MUST NOT affect them.

#### Scenario: Prices
- **WHEN** a SpeedPass and a Special Pass are bought for a 40.00 ticket after `SYSTEM PRICE;50`
- **THEN** they cost 8.00 and 40.00

#### Scenario: Composition ticket price
- **WHEN** a SpeedPass is bought for a composition ticket priced 32.00
- **THEN** it costs 6.40

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `quantity` (only 1 or 10) independent SpeedPasses identified `<purchaseIdentifier>:1` … `:<quantity>` for an existing individual ticket dated on or after the current date, and display the total price (no package discount). If any identifier is taken nothing is created.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** SP02:1 … SP02:10 exist and the total 80.00 is displayed

#### Scenario: Invalid purchase
- **WHEN** the quantity is 5, the ticket is unknown, is a group ticket, or is dated before today
- **THEN** an error is shown and no pass is created

#### Scenario: Identifier conflict
- **WHEN** SP02:3 already exists and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** an error is shown and none of SP02:1 … SP02:10 is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current date that has no Special Pass (consumed or not), and display its price.

#### Scenario: Second Special Pass rejected
- **WHEN** T001 already has a Special Pass, consumed or not
- **THEN** `SPECIALPASS CREATE;PE02;T001` is rejected

### Requirement: Pass refunds
`SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` SHALL delete a pass of the corresponding type only when it is not consumed and its ticket is dated strictly after the current date. Each pass of a package is refunded separately.

#### Scenario: Refund one pass of a package
- **WHEN** `SPEEDPASS DELETE;SP02:4` is processed for an unconsumed pass of a future ticket
- **THEN** only SP02:4 is deleted

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, its ticket is dated today or earlier, or the identifier belongs to the other pass type
- **THEN** an error is shown and the pass remains

### Requirement: Cascade with ticket deletion
A valid TICKET DELETE SHALL also delete every pass of that ticket, and a valid GROUPTICKET DELETE SHALL delete the passes of every composition ticket, the composition and the group ticket, without partial deletion.

#### Scenario: Passes removed with ticket
- **WHEN** future ticket T005 has SpeedPasses and a Special Pass bought in different operations and is deleted
- **THEN** all of those passes are deleted
