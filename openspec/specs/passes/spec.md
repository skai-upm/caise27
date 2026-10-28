# passes Specification

## Purpose
Defines personal passes attached to individual tickets — SpeedPasses and Special Passes — with their pricing, purchase, consumption state and refunds.

## Requirements

### Requirement: Pass data and identity
Every pass SHALL have its own identifier, a price, a reference to its associated individual ticket and a consumed indicator. Pass identifiers SHALL be unique among existing passes (independently of ticket identifiers). Consumed passes SHALL remain stored as purchase records.

#### Scenario: Pass identifier conflict
- **WHEN** a pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error message is displayed and no pass is created

### Requirement: Pass pricing
A SpeedPass SHALL cost 20% and a Special Pass 100% of the price stored in its associated individual ticket (the discounted price for a ticket in a group ticket), regardless of later `SYSTEM PRICE` changes.

#### Scenario: Prices from a 40.00 ticket
- **WHEN** ticket `T001` cost 40.00, `SYSTEM PRICE;50` is processed, and a SpeedPass and a Special Pass are bought for `T001`
- **THEN** the SpeedPass costs 8.00 and the Special Pass 40.00

#### Scenario: Ticket inside a group ticket
- **WHEN** a SpeedPass is bought for `TG02:12345678Z`, whose discounted price is 32.00
- **THEN** it costs 6.40

### Requirement: SPEEDPASS CREATE command
The system SHALL accept `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10, creating that many independent SpeedPasses identified `<purchaseIdentifier>:1` … `:<quantity>` for an existing individual ticket (standalone or in a group ticket) dated on or after the current system date, and display the total price (quantity × unit price, no package discount). All identifiers SHALL be checked before creating any; on conflict, an invalid quantity, an unknown ticket or a past ticket nothing SHALL be created and an error message displayed.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket dated today
- **THEN** passes `SP02:1` … `SP02:10` exist and the displayed total is 80.00

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** the pass `SP01:1` exists

#### Scenario: Partial conflict creates nothing
- **WHEN** pass `SP03:5` exists and `SPEEDPASS CREATE;SP03;T001;10` is processed
- **THEN** an error message is displayed and none of `SP03:1` … `SP03:4`, `SP03:6` … `SP03:10` is created

#### Scenario: Invalid quantity or past ticket
- **WHEN** `SPEEDPASS CREATE;SP04;T001;5` is processed, or the ticket is dated before the current date
- **THEN** an error message is displayed and no pass is created

### Requirement: SPECIALPASS CREATE command
The system SHALL accept `SPECIALPASS CREATE;<identifier>;<individualTicket>` for an existing individual ticket dated on or after the current system date that has no Special Pass (consumed or not), create it and display its price. Otherwise an error message SHALL be displayed and nothing created.

#### Scenario: Buy a Special Pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for a 40.00 ticket dated today
- **THEN** pass `PE01` exists, is not consumed, and the displayed price is 40.00

#### Scenario: Second Special Pass rejected
- **WHEN** ticket `T001` already has a Special Pass (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error message is displayed and `PE02` is not created

### Requirement: Pass refunds
The system SHALL accept `SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` and remove (refund) the pass only when it is not consumed and its ticket's date is strictly later than the current system date; each pass of a package is refunded separately. Otherwise, or for an unknown identifier or a pass of the other type, an error message SHALL be displayed and the pass kept.

#### Scenario: Refund one pass of a package
- **WHEN** `SP02:1` … `SP02:10` exist for a ticket dated after today and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** `SP02:3` no longer exists and the other nine remain

#### Scenario: Refund rejected for today's ticket or consumed pass
- **WHEN** the pass's ticket is dated today, or the pass is consumed, and its refund is requested
- **THEN** an error message is displayed and the pass still exists
