# Spec Delta

## Purpose

Defines SpeedPasses and Special Passes attached to individual tickets: purchase, pricing, consumed state and refunds (F3-C06).

## ADDED Requirements

### Requirement: Pass identity and state
Each pass SHALL be an independent object with an identifier unique among all passes, the stored price, the individual ticket it is associated with (standalone or part of a group ticket composition), and a consumed indicator. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: Pass on a group-ticket constituent
- **WHEN** a pass is bought for individual ticket `TG01:12345678Z`
- **THEN** it is associated with that constituent ticket

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the stored price of its individual ticket and a Special Pass SHALL cost that stored price, including any group discount already applied to the ticket; later `SYSTEM PRICE` changes SHALL NOT affect them.

#### Scenario: Prices from the ticket's stored price
- **WHEN** a SpeedPass and a Special Pass are bought for a 40.00 ticket after `SYSTEM PRICE;50`
- **THEN** they cost 8.00 and 40.00

#### Scenario: Discounted constituent
- **WHEN** a Special Pass is bought for a constituent ticket priced 36.00 by a 10% group discount
- **THEN** it costs 36.00

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL accept quantity 1 or 10 only and SHALL create that many SpeedPasses identified `<purchaseIdentifier>:1` to `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated on or after the current date, displaying the total charged (quantity × unit price, no package discount). If any generated identifier is already used, nothing SHALL be created.

#### Scenario: Ten-pack
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed for a 40.00 ticket
- **THEN** passes `SP02:1` to `SP02:10` exist and a total of 80.00 is displayed

#### Scenario: Invalid quantity, ticket or date
- **WHEN** quantity 2 is requested, the ticket does not exist, is a group ticket, or is dated before the current date
- **THEN** an error is reported and no pass is created

#### Scenario: Identifier conflict
- **WHEN** `SP01:1` exists and `SPEEDPASS CREATE;SP01;T001;10` is processed
- **THEN** an error is reported and none of the ten passes is created

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated on or after the current date that does not already have a Special Pass, displaying the price charged. A consumed Special Pass SHALL still prevent a second one for that ticket.

#### Scenario: Second Special Pass rejected
- **WHEN** T001 already has a Special Pass (consumed or not) and another is requested for T001
- **THEN** an error is reported and no pass is created

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL delete (refund) a pass of the matching type only when it is unconsumed and its ticket's date is strictly after the current system date. Each SpeedPass of a package SHALL be refundable separately.

#### Scenario: Refund future unused pass
- **WHEN** `SPEEDPASS DELETE;SP02:3` is processed for an unconsumed pass whose ticket is dated tomorrow
- **THEN** SP02:3 no longer exists and the other passes of SP02 remain

#### Scenario: Consumed or today's pass not refundable
- **WHEN** a refund is requested for a consumed pass or for a pass whose ticket is dated today
- **THEN** an error is reported and the pass remains stored
