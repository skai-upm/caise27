# passes Specification

## Purpose

Defines SpeedPasses and Special Passes: personal add-ons bought for an individual ticket (standalone or inside a group ticket) that grant priority or restricted access, with their pricing, refunds and lifecycle.

## Requirements

### Requirement: Pass identity and records
Every pass SHALL have an identifier unique among all passes (independent of ticket identifiers), a stored price, its associated individual ticket and a consumed flag. Consumed passes SHALL remain in the system as purchase records while their ticket exists.

#### Scenario: Consumed pass is kept
- **WHEN** a SpeedPass is consumed by entering an attraction
- **THEN** the pass still exists, marked as consumed

### Requirement: Pass pricing
A pass price SHALL be computed from the price stored in its individual ticket at purchase time: a SpeedPass costs 20 % of it and a Special Pass costs the same as it. For an individual ticket inside a group ticket, its discounted price SHALL be used. Later `SYSTEM PRICE` changes SHALL NOT affect it.

#### Scenario: SpeedPass price
- **WHEN** a SpeedPass is bought for T001 whose price is 40.00
- **THEN** the SpeedPass costs 8.00

#### Scenario: Discounted group component
- **WHEN** a SpeedPass is bought for `TG01:12345678Z` whose discounted price is 36.00
- **THEN** the SpeedPass costs 7.20

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `quantity` independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated on or after the current system date. Quantity SHALL be 1 or 10. All identifiers SHALL be checked first and nothing SHALL be created if any conflicts. The total price (quantity × unit price, no package discount) SHALL be displayed. An individual ticket MAY have any number of SpeedPasses.

#### Scenario: Package of ten
- **WHEN** `SPEEDPASS CREATE;SP01;T001;10` is processed for T001 priced 40.00 and dated today
- **THEN** passes `SP01:1` to `SP01:10` exist and the displayed total is 80.00

#### Scenario: Single pass naming
- **WHEN** `SPEEDPASS CREATE;SP05;T001;1` is processed
- **THEN** the pass `SP05:1` is created

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;SP06;T001;5` is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Identifier conflict
- **WHEN** `SP01:1` already exists and `SPEEDPASS CREATE;SP01;T001;10` is processed
- **THEN** an error is printed and none of the ten passes is created

#### Scenario: Not an individual ticket
- **WHEN** `SPEEDPASS CREATE;SP07;TG01;1` is processed for group ticket TG01
- **THEN** an error is printed

#### Scenario: Past ticket
- **WHEN** the ticket's date is before the current system date
- **THEN** the purchase is rejected

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current system date that does not already have one (consumed or not). The price charged SHALL be displayed.

#### Scenario: Buy special pass
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for T001 priced 40.00
- **THEN** PE01 is created and the displayed price is 40.00

#### Scenario: Second special pass
- **WHEN** T001 already has Special Pass PE01 (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL refund and remove an unconsumed pass whose ticket is dated strictly after the current system date. Each SpeedPass of a package SHALL be refundable separately. Consumed passes and passes of tickets for today or earlier SHALL NOT be refunded. A refunded Special Pass frees the ticket to buy another one.

#### Scenario: Refund one pass of a package
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed for an unconsumed pass of a future ticket
- **THEN** `SP02:3` no longer exists and the other passes of SP02 remain

#### Scenario: Ticket for today
- **WHEN** `SPEEDPASS DELETE;SP01:1` is processed and T001 is dated the current system date
- **THEN** an error is printed and the pass remains

#### Scenario: Consumed pass
- **WHEN** a consumed pass is deleted
- **THEN** an error is printed and the pass remains

#### Scenario: Wrong command for the pass type
- **WHEN** `SPECIALPASS DELETE;SP02:1` is processed for a SpeedPass
- **THEN** an error is printed and the pass remains

### Requirement: Passes are deleted with their ticket
When a future individual ticket is validly deleted with `TICKET DELETE`, all its SpeedPasses and its Special Pass SHALL be deleted too. When a future group ticket is validly deleted with `GROUPTICKET DELETE`, the passes of all its individual tickets, the individual tickets and the group ticket SHALL be deleted. If the deletion is not allowed, nothing SHALL change.

#### Scenario: Cascade on ticket deletion
- **WHEN** future ticket T002 has passes bought in several purchases and `TICKET DELETE;T002` succeeds
- **THEN** none of T002's passes exist afterwards

#### Scenario: Cascade on group ticket deletion
- **WHEN** future group ticket TG02 has SpeedPasses on two of its individual tickets and `GROUPTICKET DELETE;TG02` succeeds
- **THEN** those SpeedPasses, the individual tickets and TG02 no longer exist
