# pass-management Specification

## Purpose

Defines the personal passes attached to individual tickets — SpeedPasses and Special Passes — including their identity, pricing, purchase rules, consumption state and refunds.

## Requirements

### Requirement: Pass identity and state
Every pass SHALL have an identifier unique among existing passes (not shared with tickets), a price, its associated individual ticket and a consumed indicator. Consumed passes SHALL remain in the system as purchase records.

#### Scenario: New pass not consumed
- **WHEN** a pass is purchased
- **THEN** it is stored with its identifier, price, ticket and consumed = false

### Requirement: Pass prices
A SpeedPass SHALL cost 20% of the price stored in its individual ticket and a Special Pass SHALL cost the same as that price; the current SYSTEM PRICE SHALL NOT affect passes of existing tickets, and passes of composed tickets SHALL use the discounted composed price.

#### Scenario: Prices from a 40.00 ticket
- **WHEN** ticket `T001` costs `40.00`, `SYSTEM PRICE;50.00` is processed, and a SpeedPass and a Special Pass are bought for `T001`
- **THEN** the SpeedPass costs `8.00` and the Special Pass `40.00`

#### Scenario: Prices from a discounted composed ticket
- **WHEN** composed ticket `TG02:12345678Z` costs `32.00` and a Special Pass is bought for it
- **THEN** the Special Pass costs `32.00`

### Requirement: SpeedPass purchase
`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` SHALL create `quantity` independent SpeedPasses identified `<purchaseIdentifier>:1` … `<purchaseIdentifier>:<quantity>` for an existing individual ticket dated today or later. The quantity SHALL be 1 or 10. All identifiers SHALL be checked before creating any pass, and the total price (quantity × SpeedPass price, no extra discount) SHALL be displayed.

#### Scenario: Single SpeedPass
- **WHEN** the system date is `2027-04-01`, `T001` (40.00, dated `2027-04-15`) exists and `SPEEDPASS CREATE;SP01;T001;1` is processed
- **THEN** pass `SP01:1` is created and the total `8.00` is displayed

#### Scenario: Pack of ten
- **WHEN** `SPEEDPASS CREATE;SP02;T001;10` is processed
- **THEN** passes `SP02:1` … `SP02:10` are created and the total `80.00` is displayed

#### Scenario: Invalid quantity or ticket
- **WHEN** `SPEEDPASS CREATE;SP03;T001;5`, `SPEEDPASS CREATE;SP03;T999;1`, or a purchase for a ticket dated before the system date is processed
- **THEN** an error is printed and no pass is created

#### Scenario: Identifier conflict prevents partial creation
- **WHEN** pass `SP04:7` exists and `SPEEDPASS CREATE;SP04;T001;10` is processed
- **THEN** an error is printed and none of `SP04:1` … `SP04:10` other than the existing one exists

### Requirement: Special Pass purchase
`SPECIALPASS CREATE;<identifier>;<individualTicket>` SHALL create a Special Pass for an existing individual ticket dated today or later that has no Special Pass, and SHALL display its price. An individual ticket SHALL have at most one Special Pass; a consumed Special Pass still counts.

#### Scenario: Special Pass bought
- **WHEN** `SPECIALPASS CREATE;PE01;T001` is processed for ticket `T001` (40.00)
- **THEN** `PE01` is created and the price `40.00` is displayed

#### Scenario: Second Special Pass rejected
- **WHEN** `T001` already has Special Pass `PE01` (consumed or not) and `SPECIALPASS CREATE;PE02;T001` is processed
- **THEN** an error is printed and `PE02` is not created

#### Scenario: Duplicate pass identifier
- **WHEN** pass `PE01` exists and `SPECIALPASS CREATE;PE01;T002` is processed
- **THEN** an error is printed

### Requirement: Pass refunds
`SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` SHALL refund (remove) an unconsumed pass of the corresponding type whose ticket is dated strictly after the current system date. Passes of tickets for today or earlier, and consumed passes, SHALL NOT be refunded. SpeedPasses of a pack are refunded individually.

#### Scenario: Refund one pass of a pack
- **WHEN** the system date is `2027-04-01`, `SP02:3` belongs to `T001` (dated `2027-04-15`) and `SPEEDPASS DELETE;SP02:3` is processed
- **THEN** a confirmation is printed, `SP02:3` no longer exists and `SP02:4` still exists

#### Scenario: Refund not allowed
- **WHEN** the pass's ticket is dated today, the pass is consumed, the pass does not exist, or `SPECIALPASS DELETE` names a SpeedPass
- **THEN** an error is printed and nothing is removed
