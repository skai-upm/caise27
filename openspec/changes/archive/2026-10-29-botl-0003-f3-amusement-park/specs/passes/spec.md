# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes: personal add-ons bought for individual tickets (including group-composition tickets), with their pricing, consumption state and refund rules.

## ADDED Requirements

### Requirement: Pass identity and state
Every pass SHALL have its own identifier (unique among existing passes, independent of the ticket identifier space), a stored price, a reference to its individual ticket, and a consumed flag. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Consumed pass retained
- **WHEN** a SpeedPass is consumed on attraction entry
- **THEN** the pass still exists, marked as consumed

### Requirement: Pass pricing
A SpeedPass SHALL cost 20 % of the price stored in its individual ticket, and a Special Pass SHALL cost that same price. For composition tickets, this is the discounted price. Later `SYSTEM PRICE` changes SHALL NOT affect them.

#### Scenario: Prices from discounted ticket
- **WHEN** a composition ticket costs 36.00 and a SpeedPass and a Special Pass are bought for it
- **THEN** the SpeedPass costs 7.20 and the Special Pass costs 36.00

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `<quantity>` (only 1 or 10) independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated on or after the current system date, and print the total charged (quantity × unit price, with no package discount). All identifiers SHALL be checked first, and on any conflict nothing SHALL be created. An individual ticket MAY have any number of SpeedPasses.

#### Scenario: Package of ten
- **WHEN** T001 costs 40.00 and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` … `SP02:10` exist and a total of 80.00 is printed

#### Scenario: Invalid quantity or ticket
- **WHEN** `SPEEDPASS CREATE;SP03;T001;5`, a group ticket identifier, an unknown ticket, or a ticket dated before today is used
- **THEN** an error is printed and no pass is created

#### Scenario: Identifier conflict
- **WHEN** `SP02:3` already exists and `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** an error is printed and none of the ten passes is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date, and print its price. An individual ticket SHALL have at most one Special Pass, consumed or not.

#### Scenario: Second Special Pass rejected
- **WHEN** T001 already has a Special Pass (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed and no pass is created

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete a pass of that type only when it is unconsumed and its ticket's date is strictly after the current system date. Each SpeedPass of a package SHALL be refundable separately. A refunded pass SHALL no longer be persisted.

#### Scenario: Refund one pass of a package
- **WHEN** T001 is dated after today and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** `SP02:3` no longer exists and the other nine remain

#### Scenario: Refund rejected
- **WHEN** the pass is consumed, or its ticket is dated today or earlier
- **THEN** an error is printed and the pass still exists
